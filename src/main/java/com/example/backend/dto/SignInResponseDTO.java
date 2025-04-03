package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "로그인 응답 DTO")
public class SignInResponseDTO {

    @Schema(description = "로그인 성공 여부", example = "true")
    private boolean isLoggedIn;

    @Schema(description = "리다이렉트 URL", example = "/home")
    private String url;

    @Schema(description = "사용자 이름", example = "홍길동")
    private String name;

    @Schema(description = "사용자 이메일", example = "user@example.com")
    private String email;

    @Schema(description = "메시지", example = "로그인 성공")
    private String message;

    @Schema(description = "JWT Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI...")
    private String accessToken;

    @Schema(description = "Credit amount", example = "1000")
    private int credit;
}
