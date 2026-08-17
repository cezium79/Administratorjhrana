package com.administratorjhrana.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class FileStorageService {

    @Value("${file.upload-dir:uploads/photos}")
    private String uploadDir;

    public String savePhoto(byte[] data, String originalFileName) {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String extension = StringUtils.getFilenameExtension(originalFileName);
            if (extension == null) extension = "jpg";
            String newFileName = "photo_" + timestamp + "." + extension;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(newFileName);
            Files.write(filePath, data);
            log.info("Saved photo to: {}", filePath.toAbsolutePath());
            return filePath.toString();
        } catch (IOException e) {
            log.error("Failed to save photo", e);
            return null;
        }
    }
}
