package com.administratorjhrana.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class StorageService {

    private final Path rootLocation;

    public StorageService(@Value("${app.storage.path:./reports/}") String storagePath) {
        this.rootLocation = Paths.get(storagePath).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not create storage directory", e);
        }
    }

    public String saveFile(org.springframework.web.multipart.MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String uniqueFilename = UUID.randomUUID() + extension;
        Path targetLocation = rootLocation.resolve(uniqueFilename);
        try {
            Files.copy(file.getInputStream(), targetLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + originalFilename, e);
        }
        return uniqueFilename;
    }

    public Path getFile(String filename) {
        return rootLocation.resolve(filename).normalize();
    }

    public void deleteFile(String filename) {
        try {
            Path filePath = getFile(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete file " + filename, e);
        }
    }

    public List<String> getAllFilenames() {
        try {
            return Files.list(rootLocation)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    public boolean exists(String filename) {
        return Files.exists(getFile(filename));
    }

    public long getFileSize(String filename) {
        try {
            return Files.size(getFile(filename));
        } catch (IOException e) {
            return 0;
        }
    }

    public List<String> getAllFiles() {
        try {
            return Files.list(rootLocation)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .toList();
        } catch (IOException e) {
            return List.of();
        }
    }

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(".html", ".htm", ".pdf", ".txt", ".doc", ".docx");

    public boolean isAllowedExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return false;
        }
        String extension = filename.substring(filename.lastIndexOf(".")).toLowerCase();
        return ALLOWED_EXTENSIONS.contains(extension);
    }
}
