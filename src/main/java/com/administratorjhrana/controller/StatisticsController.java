package com.administratorjhrana.controller;

import com.administratorjhrana.repository.StatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsRepository statisticsRepository;

    @GetMapping("/violations")
    public ResponseEntity<List<Object[]>> getViolationsByType(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime to) {
        return ResponseEntity.ok(statisticsRepository.countViolationsByType(from, to));
    }

    @GetMapping("/checkpoints")
    public ResponseEntity<List<Object[]>> getCheckpointTimes(
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime to) {
        return ResponseEntity.ok(statisticsRepository.getCheckpointPassingTimes(from, to));
    }
}