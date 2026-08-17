package com.administratorjhrana.service;

import com.administratorjhrana.service.FileStorageService;

import com.administratorjhrana.dto.*;
import com.administratorjhrana.model.*;
import com.administratorjhrana.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;



@Service
@RequiredArgsConstructor
@Slf4j
public class ReportProcessingService {

    private final ReportRepository reportRepository;
    private final RoundRepository roundRepository;
    private final CheckpointLogRepository checkpointLogRepository;
    private final ViolationRepository violationRepository;
    private final IncidentRepository incidentRepository;
    private final FileStorageService fileStorageService;
    private final S3StorageService s3StorageService;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Transactional
    public Report saveReport(ReportSubmissionDTO dto, String folderKey) {
        // 1. Создаём Report
        Report report = new Report();
        report.setTitle(dto.getShiftId());
        report.setGuardName(dto.getEmployeeName());
        if (dto.getStartTime() != null) {
            report.setDate(parseDateTime(dto.getStartTime()));
        }
        report.setEndTime(LocalDateTime.parse(dto.getEndTime()));
        report.setHtmlContent(null); // вариант Б – оставляем null
        report.setNotes(null);

        report = reportRepository.save(report);

        // 2. Обрабатываем Round'ы
        Map<Integer, Round> roundMap = new HashMap<>();
        if (dto.getRounds() != null) {
            for (RoundDTO rDto : dto.getRounds()) {
                Round round = new Round();
                round.setRoundNumber(rDto.getRoundId());
                round.setLocation(rDto.getRouteName());
                round.setStartTime(rDto.getStartTime());
                round.setEndTime(rDto.getEndTime());
                round.setFilePath(rDto.getRouteId());
                round.setReport(report);
                round = roundRepository.save(round);
                roundMap.put(rDto.getRoundId(), round);
            }
        }

        // 3. Обрабатываем Log'и (CheckpointLog) и скачиваем фото
        if (dto.getLogs() != null) {
            for (LogDTO logDto : dto.getLogs()) {
                CheckpointLog checkpointLog = new CheckpointLog();
                checkpointLog.setCheckpointId(logDto.getCheckpointId());
                checkpointLog.setCheckpointName(logDto.getCheckpointName());
                checkpointLog.setTimestamp(parseDateTime(logDto.getTimestamp()));
                checkpointLog.setSequenceIndex(logDto.getSequenceIndex());
                checkpointLog.setIsSequenceCorrect(logDto.getIsSequenceCorrect());
                checkpointLog.setScanType(logDto.getScanType());
                checkpointLog.setActionType(logDto.getActionType());
                checkpointLog.setSequenceErrorType(logDto.getSequenceErrorType());

                // Обработка фото для лога
                String photoPath = logDto.getPhotoPath();
                if (photoPath != null && !photoPath.isEmpty()) {
                    String fileName = extractFileName(photoPath);
                    // Формируем ключ: папка смены + "photos/" + имя файла
                    String photoKey = folderKey + "photos/" + fileName;
                    if (s3StorageService.objectExists(photoKey)) {
                        byte[] photoData = s3StorageService.downloadFileAsBytes(photoKey);
                        String savedPath = fileStorageService.savePhoto(photoData, fileName);
                        checkpointLog.setPhotoPath(savedPath);
                        log.info("Downloaded photo for log: {} -> {}", photoKey, savedPath);
                    } else {
                        log.warn("Photo not found in S3: {}", photoKey);
                        checkpointLog.setPhotoPath(photoPath); // оставляем старый путь
                    }
                }

                // Привязываем к Round
                Round round = roundMap.get(logDto.getRoundId());
                if (round != null) {
                    checkpointLog.setRound(round);
                } else {
                    log.warn("Round not found for log with round_id {}", logDto.getRoundId());
                }
                checkpointLogRepository.save(checkpointLog);
            }
        }

        // 4. Обрабатываем Violation'ы
        if (dto.getViolations() != null) {
            for (ViolationDTO vDto : dto.getViolations()) {
                Violation violation = new Violation();
                violation.setType(vDto.getType());
                violation.setDescription(vDto.getDescription());
                violation.setSeverity(vDto.getSeverity());
                violation.setImageUrls(vDto.getImageUrls());
                violation.setDetectedAt(vDto.getDetectedAt());

                // Привязываем к Round, если указан, иначе оставляем null
                if (vDto.getRoundId() != null) {
                    Round round = roundMap.get(vDto.getRoundId());
                    if (round != null) {
                        violation.setRound(round);
                    } else {
                        log.warn("Round not found for violation with round_id {}", vDto.getRoundId());
                    }
                }
                // Если round_id нет или = -1, то round остаётся null
                violationRepository.save(violation);
            }
        }

        // 5. Обрабатываем Incident'ы и скачиваем их фото
        if (dto.getIncidents() != null) {
            for (IncidentDTO iDto : dto.getIncidents()) {
                Incident incident = new Incident();
                incident.setTimestamp(iDto.getTimestamp());
                incident.setIncidentType(iDto.getIncidentType());
                incident.setDescription(iDto.getDescription());

                // Скачиваем фото инцидента
                String photoPath = iDto.getPhotoPath();
                if (photoPath != null && !photoPath.isEmpty()) {
                    String fileName = extractFileName(photoPath);
                    String photoKey = folderKey + "photos/" + fileName;
                    if (s3StorageService.objectExists(photoKey)) {
                        byte[] photoData = s3StorageService.downloadFileAsBytes(photoKey);
                        String savedPath = fileStorageService.savePhoto(photoData, fileName);
                        incident.setPhotoPath(savedPath);
                        log.info("Downloaded photo for incident: {} -> {}", photoKey, savedPath);
                    } else {
                        log.warn("Incident photo not found: {}", photoKey);
                        incident.setPhotoPath(photoPath);
                    }
                }

                incident.setStatus("NEW"); // статус по умолчанию

                // Привязываем к Round, если round_id != -1
                if (iDto.getRoundId() != null && iDto.getRoundId() != -1) {
                    Round round = roundMap.get(iDto.getRoundId());
                    if (round != null) {
                        incident.setRound(round);
                    } else {
                        log.warn("Round not found for incident with round_id {}", iDto.getRoundId());
                    }
                }
                // violation не привязываем
                incidentRepository.save(incident);
            }
        }

        log.info("Saved report with ID: {}", report.getId());
        return report;
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null) return null;
        try {
            return LocalDateTime.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            log.warn("Could not parse datetime: {}", dateStr);
            return null;
        }
    }

    /**
     * Извлекает имя файла из полного пути (учитывает как Unix, так и Windows-разделители).
     */
    private String extractFileName(String path) {
        if (path == null) return null;
        int lastSlash = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }
}