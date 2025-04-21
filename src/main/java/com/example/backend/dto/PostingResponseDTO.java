package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "채용공고 매칭 결과 DTO")
public class PostingResponseDTO {

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

    // 추가 필드
    private LocalDate startDay;
    private LocalDate endDay;
    private String name;
    private String uri;
}
