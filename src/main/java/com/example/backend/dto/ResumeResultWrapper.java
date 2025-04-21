package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "이력서 처리 결과 래퍼 DTO")
public class ResumeResultWrapper {

    @Schema(description = "처리 결과 상태", example = "성공", required = true)
    private OneToneDTO result;

    @JsonProperty("object_id")
    private String objectId;
}
