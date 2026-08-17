package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoundDTO {
    @JsonProperty("round_id")
    private Integer roundId;

    @JsonProperty("start_time")
    private String startTime;

    @JsonProperty("end_time")
    private String endTime;

    @JsonProperty("route_id")
    private String routeId;

    @JsonProperty("route_name")
    private String routeName;

    @JsonProperty("checkpoints_count")
    private Integer checkpointsCount;

    @JsonProperty("checkpoints_passed")
    private Integer checkpointsPassed;

    @JsonProperty("sequence_violations")
    private Integer sequenceViolations;


}