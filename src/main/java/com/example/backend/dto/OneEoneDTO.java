package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "1:1 이력서-채용공고 매칭 결과 DTO")
public class OneEoneDTO {

    @Schema(description = "요약", example = "지원자의 경험과 직무 요구사항이 전반적으로 잘 일치합니다.")
    private String summary;

    @Schema(description = "총점", example = "87.5")
    private double total_score;

    @Schema(description = "GPT 분석 결과", example = "이 지원자는 백엔드 직무에 적합합니다. 특히 Django와 AWS 경험이 강점입니다.")
    private String gpt_answer;
}
