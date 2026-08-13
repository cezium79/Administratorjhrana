package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ViolationDTO {
    private String type;
    private String description;
    private String severity;
    @JsonFormat(pattern = "dd.MM.yyyy HH:mm:ss") private LocalDateTime detectedAt;
}