package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LogEntryDTO {
    @JsonProperty("checkpoint_name") private String checkpointName;
    @JsonProperty("checkpoint_id") private String checkpointId;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss") private LocalDateTime timestamp;
    private Integer roundId;
    private String routeName;
    @JsonProperty("sequence_index") private Integer sequenceIndex;
    @JsonProperty("is_sequence_correct") private Boolean isSequenceCorrect;
    private String scanType;
    private String actionType;
    @JsonProperty("sequence_error_type") private String sequenceErrorType;

    // Динамические поля в зависимости от action_type
    private String inputValue;
    private String photoPath;
    private String answer;
}
