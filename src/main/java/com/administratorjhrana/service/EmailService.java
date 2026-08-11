package com.administratorjhrana.service;

import com.administratorjhrana.model.Report;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ReportService reportService;

    @Autowired
    public EmailService(JavaMailSender mailSender, ReportService reportService) {
        this.mailSender = mailSender;
        this.reportService = reportService;
    }

    public void sendReportToEmail(Long reportId, String toEmail) {
        Report report = reportService.getReportById(reportId);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom("noreply@jhrana.com");
            helper.setTo(toEmail);
            helper.setSubject("Отчёт обхода: " + (report.getTitle() != null ? report.getTitle() : "Без названия"));

            String content = "Добрый день!\n\n"
                    + "Направляю вам отчёт обхода:\n\n"
                    + "Охранник: " + (report.getGuardName() != null ? report.getGuardName() : "Не указан") + "\n"
                    + "Дата: " + (report.getDate() != null ? report.getDate() : "Не указана") + "\n"
                    + "Название: " + (report.getTitle() != null ? report.getTitle() : "Без названия") + "\n";

            if (report.getNotes() != null && !report.getNotes().isEmpty()) {
                content += "Примечания: " + report.getNotes() + "\n";
            }

            content += "\nС уважением,\nСистема контроля обходов";
            helper.setText(content);

            if (report.getFilePath() != null) {
                FileSystemResource attachment = new FileSystemResource(reportService.getStorageService().getFile(report.getFilePath()));
                if (attachment.exists()) {
                    helper.addAttachment(report.getFilePath(), attachment);
                }
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Could not send email", e);
        }
    }
}
