package com.administratorjhrana.controller;

import com.administratorjhrana.model.Report;
import com.administratorjhrana.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/search")
    public ResponseEntity<List<Report>> searchReports(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchByQuery(query));
    }
}