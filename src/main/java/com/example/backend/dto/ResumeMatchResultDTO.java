package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "이력서 → 채용공고 매칭 결과 DTO")
public class ResumeMatchResultDTO {

    @Schema(description = "채용공고 제목", example = "백엔드 개발자 (Python/Django)")
    private String title;

    @Schema(description = "요약", example = "지원자의 기술과 직무 요구가 전반적으로 일치합니다.")
    private String summary;

    @Schema(description = "총점", example = "91.2")
    private double total_score;

    @Schema(description = "GPT 분석 결과", example = "이 지원자는 백엔드 포지션에 매우 적합합니다. 특히 Django 경험이 강점입니다.")
    private String gpt_answer;
}