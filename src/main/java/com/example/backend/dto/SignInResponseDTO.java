package com.example.backend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInResponseDTO {
    private boolean isLoggedIn;
    private String url;
    private String name;
    private String email;
    private String message;
    private String jwtToken;
}
