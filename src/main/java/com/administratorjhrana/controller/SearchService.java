package com.administratorjhrana.service;

import com.administratorjhrana.model.Report;
import com.administratorjhrana.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final ReportRepository reportRepository;

    @Transactional(readOnly = true)
    public List<Report> searchByQuery(String query) {
        String pattern = "%" + query.toLowerCase() + "%";
        return reportRepository.findByTitleContainingIgnoreCaseOrNotesContainingIgnoreCaseOrGuardNameContainingIgnoreCase(
                pattern, pattern, pattern);
    }
}