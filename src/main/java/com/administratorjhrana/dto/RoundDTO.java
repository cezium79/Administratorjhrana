package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RoundDTO {
    private Integer roundId;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss") private LocalDateTime startTime;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss") private LocalDateTime endTime;
    private String routeId;
    private String routeName;
    private Integer checkpointsCount;
    private Integer checkpointsPassed;
    private Integer sequenceViolations;
}