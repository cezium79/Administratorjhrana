package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReportSubmissionDTO {
    @JsonProperty("shift_id") private String shiftId;
    @JsonProperty("employee_name") private String employeeName;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss")
    private LocalDateTime endTime;

    @JsonProperty("strict_sequence_enabled") private Boolean strictSequenceEnabled;

    private List<RoundDTO> rounds;
    private List<LogEntryDTO> logs;
    private List<ViolationDTO> violations;
}