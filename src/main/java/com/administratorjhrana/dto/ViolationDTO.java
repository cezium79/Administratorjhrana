package com.administratorjhrana.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ViolationDTO {
    private String type;
    private String description;
    private String severity;
    @JsonProperty("image_urls")
    private String imageUrls;
    @JsonProperty("detected_at")
    private String detectedAt;
    @JsonProperty("round_id")   // добавили для привязки
    private Integer roundId;
}