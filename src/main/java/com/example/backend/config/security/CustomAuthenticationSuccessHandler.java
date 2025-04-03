package com.example.backend.config.security;

import com.example.backend.config.jwt.JwtUtil;
import com.example.backend.token.GeneratedToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import com.example.backend.dto.SignInResponseDTO;
import com.example.backend.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();

        GeneratedToken token = jwtUtil.generateToken(user); // access + refresh

        // refreshToken을 HttpOnly 쿠키로 저장
        Cookie refreshCookie = new Cookie("refreshToken", token.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setSecure(true); // HTTPS만 허용 (로컬에서는 false로 해도 OK)
        refreshCookie.setPath("/"); // 모든 요청에 대해 자동 전송
        refreshCookie.setMaxAge(60 * 60 * 24 * 14); // 2주

        response.addCookie(refreshCookie);

        // accessToken만 프론트에 JSON으로 전달
        SignInResponseDTO responseDTO = SignInResponseDTO.builder()
                .accessToken(token.getAccessToken())
                .isLoggedIn(true)
                .message("로그인 성공")
                .email(user.getEmail())
                .name(user.getName())
                .build();

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(responseDTO));
    }

}
