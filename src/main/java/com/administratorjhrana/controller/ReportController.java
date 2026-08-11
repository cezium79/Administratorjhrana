package com.administratorjhrana.controller;

import com.administratorjhrana.dto.ReportDTO;
import com.administratorjhrana.model.Report;
import com.administratorjhrana.service.EmailService;
import com.administratorjhrana.service.ReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private final EmailService emailService;

    @Autowired
    public ReportController(ReportService reportService, EmailService emailService) {
        this.reportService = reportService;
        this.emailService = emailService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Report> uploadReport(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "guardName", required = false) String guardName,
            @RequestParam(value = "date", required = false) String dateStr,
            @RequestParam(value = "notes", required = false) String notes) {

        ReportDTO dto = new ReportDTO();
        dto.setTitle(title);
        dto.setGuardName(guardName);
        dto.setNotes(notes);
        if (dateStr != null && !dateStr.isEmpty()) {
            dto.setDate(LocalDateTime.parse(dateStr));
        }

        Report report = reportService.saveReport(dto, file);
        return ResponseEntity.ok(report);
    }

    @PostMapping
    public ResponseEntity<Report> uploadReportFromUrl(
            @RequestBody Map<String, String> request) {

        String fileUrl = request.get("url");
        String title = request.get("title");
        String guardName = request.get("guardName");
        String dateStr = request.get("date");
        String notes = request.get("notes");

        if (fileUrl == null || fileUrl.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ReportDTO dto = new ReportDTO();
        dto.setTitle(title);
        dto.setGuardName(guardName);
        dto.setNotes(notes);
        if (dateStr != null && !dateStr.isEmpty()) {
            dto.setDate(LocalDateTime.parse(dateStr));
        }

        Report report = reportService.saveReportFromUrl(dto, fileUrl);
        return ResponseEntity.ok(report);
    }

    @GetMapping
    public ResponseEntity<Page<Report>> getReports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "uploadedAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String guardName,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) LocalDateTime dateFrom,
            @RequestParam(required = false) LocalDateTime dateTo) {

        Page<Report> reports = reportService.getReports(page, size, sortBy, direction, guardName, title, dateFrom, dateTo);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Report> getReport(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        return ResponseEntity.ok(report);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Report> updateReport(@PathVariable Long id, @RequestBody ReportDTO dto) {
        Report report = reportService.updateReport(id, dto);
        return ResponseEntity.ok(report);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        reportService.deleteReport(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/html")
    public ResponseEntity<Resource> getHtml(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        if (report.getHtmlUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = reportService.getStorageService().getFile(report.getHtmlUrl());
        FileSystemResource resource = new FileSystemResource(filePath.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + report.getHtmlUrl() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> getPdf(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        if (report.getPdfUrl() == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = reportService.getStorageService().getFile(report.getPdfUrl());
        FileSystemResource resource = new FileSystemResource(filePath.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + report.getPdfUrl() + "\"")
                .body(resource);
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Report report = reportService.getReportById(id);
        if (report.getFilePath() == null) {
            return ResponseEntity.notFound().build();
        }
        Path filePath = reportService.getStorageService().getFile(report.getFilePath());
        FileSystemResource resource = new FileSystemResource(filePath.toFile());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + report.getFilePath() + "\"")
                .body(resource);
    }

    @PostMapping("/{id}/email")
    public ResponseEntity<Map<String, String>> sendByEmail(@PathVariable Long id,
                                                           @RequestBody Map<String, String> request) {
        String toEmail = request.get("email");
        if (toEmail == null || toEmail.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            emailService.sendReportToEmail(id, toEmail);
            return ResponseEntity.ok(Map.of("message", "Email sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send email: " + e.getMessage()));
        }
    }

    @GetMapping("/filters")
    public ResponseEntity<Map<String, List<String>>> getFilters() {
        List<String> guardNames = reportService.getDistinctGuardNames();
        List<String> titles = reportService.getDistinctTitles();
        return ResponseEntity.ok(Map.of(
                "guardNames", guardNames,
                "titles", titles
        ));
    }
}
