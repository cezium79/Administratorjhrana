package com.administratorjhrana.scheduler;

import com.administratorjhrana.dto.ReportSubmissionDTO;
import com.administratorjhrana.service.ReportProcessingService;
import com.administratorjhrana.service.S3StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final S3StorageService s3StorageService;
    private final ReportProcessingService reportProcessingService;
    private final ObjectMapper objectMapper;

    private static final String REPORTS_PREFIX = "reports/";
    private static final String PROCESSED_PREFIX = "processed/";
    private static final String JSON_FILE_NAME = "report.json";

    @Scheduled(fixedDelay = 300000) // 5 минут
    public void processReports() {
        log.info("Starting scheduled processing of new reports from S3...");
        List<String> folders = s3StorageService.listFolders(REPORTS_PREFIX);
        log.info("Found {} folders", folders.size());

        for (String folder : folders) {
            try {
                processFolder(folder);
            } catch (Exception e) {
                log.error("Failed to process folder: {}", folder, e);
            }
        }
    }

    private void processFolder(String folderKey) {
        String markerKey = folderKey + ".processed";
        if (s3StorageService.objectExists(markerKey)) {
            log.info("Folder {} already processed, skipping", folderKey);
            return;
        }

        String jsonKey = folderKey + JSON_FILE_NAME;
        if (!s3StorageService.objectExists(jsonKey)) {
            log.warn("No report.json in folder {}", folderKey);
            return;
        }

        String jsonContent = s3StorageService.downloadFileAsString(jsonKey);
        ReportSubmissionDTO dto;
        try {
            dto = objectMapper.readValue(jsonContent, ReportSubmissionDTO.class);
        } catch (IOException e) {
            log.error("Failed to parse JSON from {}: {}", jsonKey, e.getMessage());
            return;
        }

        // Сохраняем в БД (передаём folderKey для скачивания фото)
        reportProcessingService.saveReport(dto, folderKey);

        // Помечаем как обработанное
        s3StorageService.uploadEmptyObject(markerKey);

        // Перемещаем файлы в processed/
        moveFolderToProcessed(folderKey);

        log.info("Successfully processed folder: {}", folderKey);
    }

    private void moveFolderToProcessed(String folderKey) {
        List<String> files = s3StorageService.listFiles(folderKey);
        for (String fileKey : files) {
            String destinationKey = fileKey.replace(REPORTS_PREFIX, PROCESSED_PREFIX);
            s3StorageService.moveObject(fileKey, destinationKey);
        }
    }
}