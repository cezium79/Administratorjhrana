package com.administratorjhrana.service;

import com.administratorjhrana.dto.ReportDTO;
import com.administratorjhrana.model.Report;
import jakarta.mail.*;
import jakarta.mail.search.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class ImapReportReceiver {

    private static final Logger logger = Logger.getLogger(ImapReportReceiver.class.getName());

    private final ReportService reportService;
    private final StorageService storageService;

    private static final String IMAP_HOST = "imap.yandex.ru";
    private static final int IMAP_PORT = 993;
    private static final String USERNAME = "belkinnikola2@yandex.ru";
    private static final String PASSWORD = "iybtkrxwtdhxsmzo";
    //private static final String ALLOWED_SENDER = "Ohrana-App1.1@yandex.ru"; // Замените на нужный адрес

    @Autowired
    public ImapReportReceiver(ReportService reportService, StorageService storageService) {
        this.reportService = reportService;
        this.storageService = storageService;
    }

    @Transactional
    public Map<String, Object> checkEmailsManually() {
        logger.info("Ручная проверка почты...");
        try {
            return checkNewEmails();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка проверки почты", e);
            return Map.of("status", "error", "message", e.getMessage());
        }
    }

    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void scheduledCheck() {
        logger.info("Автопроверка почты...");
        try {
            checkNewEmails();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Ошибка автопроверки", e);
        }
    }

    private Map<String, Object> checkNewEmails() throws MessagingException {
        Properties props = new Properties();
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.imap.port", String.valueOf(IMAP_PORT));
        props.put("mail.imap.host", IMAP_HOST);

        Session session = Session.getInstance(props);

        Store store = session.getStore("imap");
        store.connect(USERNAME, PASSWORD);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_WRITE);

        // Стало:
// Создаем список всех разрешенных отправителей
        SearchTerm[] senders = {
                new FromStringTerm("Ohrana-App1.1@yandex.ru"),
                new FromStringTerm("belkinnikola@yandex.ru"),
                new FromStringTerm("belkinnikola@gmail.com"),
                new FromStringTerm("belkinnikola@rambler.ru")
        };

// Ищем письма, которые одновременно:
// 1. Не прочитаны (SEEN = false)
// 2. Пришли от кого-либо из списка выше
        SearchTerm unSeenTerm = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
        SearchTerm fromOrTerm = new OrTerm(senders);
        AndTerm combinedTerm = new AndTerm(unSeenTerm, fromOrTerm);

        Message[] messages = inbox.search(combinedTerm);
        logger.info("Найдено непрочитанных писем: " + messages.length);

        int count = 0;
        List<String> files = new ArrayList<>();

        for (Message msg : messages) {
            try {
                ReportDTO dto = parseEmail(msg);
                if (dto != null) {
                    String attachmentFile = saveAttachment(msg);
                    if (attachmentFile != null) {
                        Report report = reportService.saveReportFromFile(dto, attachmentFile);
                        files.add(report.getTitle());
                        count++;
                    } else {
                        logger.info("Письмо без вложения: " + msg.getSubject());
                    }
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Ошибка письма: " + msg.getSubject(), e);
            }
        }

        for (Message msg : messages) {
            msg.setFlag(Flags.Flag.SEEN, true);
        }

        inbox.close(true);
        store.close();

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("processedCount", count);
        result.put("files", files);
        result.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        logger.info("Проверка завершена. Писем: " + count);
        return result;
    }

    private ReportDTO parseEmail(Message message) throws MessagingException, IOException {
        ReportDTO dto = new ReportDTO();
        String subject = message.getSubject();
        logger.info("Письмо: " + subject);

        // Простая логика: ставим текущую дату, если не удалось распарсить

        if (subject != null && subject.contains("—")) {
            String[] parts = subject.split("—");
            if (parts.length >= 3) {
                dto.setGuardName(parts[1].trim()); // Если есть имя, ставим его
                try {
                    LocalDate date = LocalDate.parse(parts[2].trim(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                    dto.setDate(date.atTime(9, 0));
                } catch (DateTimeParseException e) {
                    dto.setDate(LocalDateTime.now());
                }
            }
        } else {
            // Если нет тире или тема пустая, ставим текущие значения по умолчанию
            dto.setGuardName("Unknown"); // или dto.setGuardName(null);
            dto.setDate(LocalDateTime.now());
        }

        Object content = message.getContent();
        if (content instanceof String) {
            dto.setNotes((String) content);
        } else if (content instanceof Multipart) {
            Multipart mp = (Multipart) content;
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    dto.setNotes(bp.getContent().toString());
                    break;
                }
            }
        }
        return dto;
    }

    private String saveAttachment(Message message) throws MessagingException, IOException {
        Object content = message.getContent();
        if (content instanceof Multipart) {
            Multipart mp = (Multipart) content;
            for (int i = 0; i < mp.getCount(); i++) {
                BodyPart bp = mp.getBodyPart(i);
                String filename = bp.getFileName();
                if (filename != null) {
                    String lower = filename.toLowerCase();
                    if (lower.endsWith(".html") || lower.endsWith(".htm") || lower.endsWith(".pdf")) {
                        String unique = UUID.randomUUID().toString() + lower.substring(lower.lastIndexOf("."));
                        Path targetPath = storageService.getFile(unique);
                        try (InputStream is = bp.getInputStream()) {
                            Files.copy(is, targetPath, StandardCopyOption.REPLACE_EXISTING);
                            logger.info("Сохранено вложение: " + filename);
                            return unique;
                        }
                    }
                }
            }
        }
        return null;
    }
}