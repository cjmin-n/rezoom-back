package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이력서 매칭 결과 DTO")
public class ResumeResponseDTO {

    @JsonProperty("total_score")
    private String totalScore;

    @JsonProperty("resume_score")
    private String resumeScore;

    @JsonProperty("selfintro_score")
    private String selfintroScore;

    @JsonProperty("opinion1")
    private String opinion1;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("eval_resume")
    private String evalResume;

    @JsonProperty("eval_selfintro")
    private String evalSelfintro;

    @Schema(description = "지원자의 이름", example = "김민수", required = true)
    private String name;

    @Schema(description = "이력서 파일 URI", example = "/files/abc123xyz.pdf", required = true)
    private String uri;

    @Schema(description = "이력서 생성 일시", example = "2025-03-01T10:15:30", required = true)
    private LocalDateTime created_at;
}
