package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class TokenRefreshRequestDTO {
    private int status;
    private String message;
    private String refreshToken;
    private String accessToken;
}