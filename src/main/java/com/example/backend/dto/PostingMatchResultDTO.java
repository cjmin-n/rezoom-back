package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "채용공고 → 이력서 매칭 결과 DTO")
public class PostingMatchResultDTO {

    @Schema(description = "이름", example = "김민수")
    private String name;

    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phone;

    @Schema(description = "이메일", example = "minsu@example.com")
    private String email;

    @Schema(description = "보유 기술 목록", example = "[\"Python\", \"Spring\", \"Docker\"]")
    private List<String> skills;

    @Schema(description = "학력", example = "서울대학교 컴퓨터공학과")
    private String education;

    @Schema(description = "경력", example = "카카오에서 백엔드 엔지니어로 3년 근무")
    private String experience;

    @Schema(description = "자기소개", example = "성실함과 도전정신으로 백엔드 개발자로서 성장해왔습니다.")
    private String self_intro;

    @Schema(description = "유사도 점수", example = "81.3")
    private double similarity_score;
}
