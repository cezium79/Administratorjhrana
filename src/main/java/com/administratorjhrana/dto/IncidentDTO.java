package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IncidentDTO {
    private String timestamp;
    @JsonProperty("shift_id")
    private String shiftId;
    @JsonProperty("round_id")
    private Integer roundId;
    @JsonProperty("employee_name")
    private String employeeName;
    @JsonProperty("incident_type")
    private String incidentType;
    private String description;
    @JsonProperty("photo_path")
    private String photoPath;
}