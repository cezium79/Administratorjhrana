package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LogDTO {
    @JsonProperty("checkpoint_name")
    private String checkpointName;

    @JsonProperty("checkpoint_id")
    private String checkpointId;

    private String timestamp;

    @JsonProperty("round_id")
    private Integer roundId;

    @JsonProperty("route_name")
    private String routeName;

    @JsonProperty("sequence_index")
    private Integer sequenceIndex;

    @JsonProperty("is_sequence_correct")
    private Boolean isSequenceCorrect;

    @JsonProperty("scan_type")
    private String scanType;

    @JsonProperty("action_type")
    private String actionType;

    @JsonProperty("photo_path")
    private String photoPath;

    @JsonProperty("sequence_error_type")
    private String sequenceErrorType;

    @JsonProperty("input_value")
    private String inputValue;

    private String answer;
}