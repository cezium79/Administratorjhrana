package com.administratorjhrana.service;

import com.administratorjhrana.dto.ReportDTO;
import com.administratorjhrana.model.CheckpointLog;
import com.administratorjhrana.model.Report;
import com.administratorjhrana.model.Round;
import com.administratorjhrana.model.Violation;
import com.administratorjhrana.repository.ReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.administratorjhrana.dto.*;
import lombok.RequiredArgsConstructor;



@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final StorageService storageService;

    @Autowired
    public ReportService(ReportRepository reportRepository, StorageService storageService) {
        this.reportRepository = reportRepository;
        this.storageService = storageService;
    }

    @Transactional
    public Report saveReport(ReportDTO dto, MultipartFile file) {
        Report report = new Report();
        updateReportFromDTO(report, dto);
        if (file != null && !file.isEmpty()) {
            String filename = storageService.saveFile(file);
            report.setFilePath(filename);
            report.setSize(file.getSize());
            String lowerName = filename.toLowerCase();
            if (lowerName.endsWith(".pdf")) {
                report.setPdfUrl(filename);
            } else if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
                report.setHtmlUrl(filename);
                try {
                    report.setHtmlContent(readFileContent(filename));
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        report.setDate(dto.getDate() != null ? dto.getDate() : LocalDateTime.now());
        report.setTitle(dto.getTitle() != null ? dto.getTitle() : "Отчёт от " + LocalDateTime.now().toLocalDate());
        report.setGuardName(dto.getGuardName());
        report.setNotes(dto.getNotes());
        return reportRepository.save(report);
    }
    @Transactional
    public Report saveReportFromDto(ReportSubmissionDTO dto) {
        Report report = new Report();
        report.setTitle("Отчет: " + dto.getShiftId());
        report.setGuardName(dto.getEmployeeName());
        report.setDate(dto.getStartTime());
        report.setNotes("Strict sequence: " + dto.getStrictSequenceEnabled());

        // Создание раундов
        List<Round> rounds = new ArrayList<>();
        if (dto.getRounds() != null) {
            for (RoundDTO roundDto : dto.getRounds()) {
                Round round = new Round();
                round.setRoundNumber(roundDto.getRoundId());
                round.setLocation(roundDto.getRouteName());

                // Обработка нарушений раунда
                if (roundDto.getSequenceViolations() != null && roundDto.getSequenceViolations() > 0) {
                    Violation violation = new Violation();
                    violation.setType("SEQUENCE_BREACH");
                    violation.setDescription("Нарушение: " + roundDto.getSequenceViolations());
                    violation.setSeverity("HIGH");
                    round.addViolation(violation);
                }
                rounds.add(round);
                report.addRound(round);
            }
        }

        // Создание логов (CheckpointLog)
        if (dto.getLogs() != null) {
            for (LogEntryDTO logDto : dto.getLogs()) {
                CheckpointLog log = new CheckpointLog();
                log.setCheckpointId(logDto.getCheckpointId());
                log.setCheckpointName(logDto.getCheckpointName());
                log.setTimestamp(logDto.getTimestamp());
                log.setRouteName(logDto.getRouteName());
                log.setSequenceIndex(logDto.getSequenceIndex());
                log.setIsSequenceCorrect(logDto.getIsSequenceCorrect());
                log.setScanType(logDto.getScanType());
                log.setActionType(logDto.getActionType());
                log.setSequenceErrorType(logDto.getSequenceErrorType());
                log.setInputValue(logDto.getInputValue());
                log.setPhotoPath(logDto.getPhotoPath());
                log.setAnswer(logDto.getAnswer());

                // Привязка к раунду по ID
                if (logDto.getRoundId() != null && rounds != null) {
                    Round matchedRound = rounds.stream()
                            .filter(r -> r.getRoundNumber().equals(logDto.getRoundId()))
                            .findFirst().orElse(null);
                    if (matchedRound != null) {
                        log.setRound(matchedRound);
                        matchedRound.addLog(log);
                    }
                }
            }
        }

        return reportRepository.save(report);
    }
    @Transactional
    public Report saveReportFromUrl(ReportDTO dto, String fileUrl) {
        Report report = new Report();
        updateReportFromDTO(report, dto);

        try {
            String extension = "";
            if (fileUrl.contains(".")) {
                extension = fileUrl.substring(fileUrl.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + extension;
            Path targetPath = storageService.getFile(filename);
            try (InputStream in = new URL(fileUrl).openStream()) {
                Files.copy(in, targetPath);
            }
            report.setFilePath(filename);
            report.setSize(Files.size(targetPath));
            String lowerName = filename.toLowerCase();
            if (lowerName.endsWith(".pdf")) {
                report.setPdfUrl(filename);
            } else if (lowerName.endsWith(".html") || lowerName.endsWith(".htm")) {
                report.setHtmlUrl(filename);
                report.setHtmlContent(readFileContentFromUrl(fileUrl));
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not download file from URL: " + fileUrl, e);
        }

        report.setDate(dto.getDate() != null ? dto.getDate() : LocalDateTime.now());
        report.setTitle(dto.getTitle() != null ? dto.getTitle() : "Отчёт от " + LocalDateTime.now().toLocalDate());
        report.setGuardName(dto.getGuardName());
        report.setNotes(dto.getNotes());
        return reportRepository.save(report);
    }

    private String readFileContent(String filename) throws IOException {
        Path path = storageService.getFile(filename);
        String content = Files.readString(path, StandardCharsets.UTF_8);
        if (content.length() > 50000) {
            content = content.substring(0, 50000);
        }
        return content;
    }

    private String readFileContentFromUrl(String fileUrl) throws IOException {
        String content = new String(new URL(fileUrl).openStream().readAllBytes(), StandardCharsets.UTF_8);
        if (content.length() > 50000) {
            content = content.substring(0, 50000);
        }
        return content;
    }

    private void updateReportFromDTO(Report report, ReportDTO dto) {
        if (dto.getTitle() != null) {
            report.setTitle(dto.getTitle());
        }
        if (dto.getGuardName() != null) {
            report.setGuardName(dto.getGuardName());
        }
        if (dto.getNotes() != null) {
            report.setNotes(dto.getNotes());
        }
    }

    @Transactional(readOnly = true)
    public Page<Report> getReports(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("DESC")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return reportRepository.findAllByOrderByUploadedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Report> getReports(int page, int size, String sortBy, String direction,
                                   String guardName, String title, LocalDateTime dateFrom, LocalDateTime dateTo) {
        Pageable pageable = PageRequest.of(page, size,
                direction.equalsIgnoreCase("DESC")
                        ? Sort.by(sortBy).descending()
                        : Sort.by(sortBy).ascending());

        if (dateFrom != null && dateTo != null) {
            return reportRepository.findByDateBetween(dateFrom, dateTo, pageable);
        }
        if (guardName != null && !guardName.isEmpty()) {
            return reportRepository.findByGuardNameContainingIgnoreCase(guardName, pageable);
        }
        if (title != null && !title.isEmpty()) {
            return reportRepository.findByTitleContainingIgnoreCase(title, pageable);
        }
        return reportRepository.findAllByOrderByUploadedAtDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Report getReportById(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Report not found with id: " + id));
    }

    @Transactional
    public Report updateReport(Long id, ReportDTO dto) {
        Report report = getReportById(id);
        if (dto.getTitle() != null) {
            report.setTitle(dto.getTitle());
        }
        if (dto.getGuardName() != null) {
            report.setGuardName(dto.getGuardName());
        }
        if (dto.getDate() != null) {
            report.setDate(dto.getDate());
        }
        if (dto.getNotes() != null) {
            report.setNotes(dto.getNotes());
        }
        return reportRepository.save(report);
    }

    @Transactional
    public Report saveReportFromFile(ReportDTO dto, String filePath) {
        Report report = new Report();
        report.setTitle(dto.getTitle() != null ? dto.getTitle() : "Отчёт от " + LocalDateTime.now().toLocalDate());
        report.setGuardName(dto.getGuardName());
        report.setDate(dto.getDate() != null ? dto.getDate() : LocalDateTime.now());
        report.setNotes(dto.getNotes());
        report.setFilePath(filePath);
        
        String lowerFilename = filePath.toLowerCase();
        if (lowerFilename.endsWith(".pdf")) {
            report.setPdfUrl(filePath);
        } else if (lowerFilename.endsWith(".html") || lowerFilename.endsWith(".htm")) {
            report.setHtmlUrl(filePath);
            try {
                report.setHtmlContent(Files.readString(storageService.getFile(filePath), StandardCharsets.UTF_8));
                if (report.getHtmlContent().length() > 50000) {
                    report.setHtmlContent(report.getHtmlContent().substring(0, 50000));
                }
            } catch (IOException e) {
                // ignore
            }
        }
        
        try {
            report.setSize(Files.size(storageService.getFile(filePath)));
        } catch (IOException e) {
            // ignore
        }
        
        return reportRepository.save(report);
    }

    @Transactional
    public void deleteReport(Long id) {
        Report report = getReportById(id);
        if (report.getFilePath() != null) {
            storageService.deleteFile(report.getFilePath());
        }
        reportRepository.delete(report);
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctGuardNames() {
        return reportRepository.findDistinctGuardNames();
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctTitles() {
        return reportRepository.findDistinctTitles();
    }

    public StorageService getStorageService() {
        return storageService;
    }
}
