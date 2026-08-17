package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class ReportSubmissionDTO {
    @JsonProperty("shift_id")
    private String shiftId;

    @JsonProperty("employee_name")
    private String employeeName;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("strict_sequence_enabled")
    private Boolean strictSequenceEnabled;

    private List<RoundDTO> rounds;
    private List<LogDTO> logs;
    private List<ViolationDTO> violations;
    private List<IncidentDTO> incidents;



}