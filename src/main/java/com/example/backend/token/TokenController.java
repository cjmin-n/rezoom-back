package com.example.backend.token;

import com.example.backend.config.jwt.JwtUtil;
import com.example.backend.dto.TokenRefreshRequestDTO;
import com.example.backend.dto.sign.SignInResponseDTO;
import com.example.backend.entity.User;
import com.example.backend.swagger.TokenControllerDocs;
import com.example.backend.user.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/token")
public class TokenController implements TokenControllerDocs {

    private final RefreshTokenService refreshTokenService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader, HttpServletResponse response) {

        try {
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                authHeader = authHeader.substring(7);
            }

            String email = jwtUtil.getUid(authHeader);
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalStateException("회원이 존재하지 않습니다."));

            // Refresh Token 삭제
            refreshTokenService.deleteRefreshToken(user);
            log.info("{}님의 refreshToken 로그아웃 완료", user.getEmail());

            // 쿠키 만료 (refreshToken이 쿠키로 관리 중일 경우)
            Cookie refreshCookie = new Cookie("refreshToken", null);
            refreshCookie.setHttpOnly(true);
            refreshCookie.setSecure(true);
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(0);
            response.addCookie(refreshCookie);

            // SameSite=None 명시해서 완전 삭제
            String deleteCookieHeader = "refreshToken=; Max-Age=0; Path=/; Secure; HttpOnly; SameSite=None";
            response.setHeader("Set-Cookie", deleteCookieHeader);


            return ResponseEntity.ok(Map.of("message", "Logout successful."));
        } catch (Exception e) {
            log.error("로그아웃 중 에러", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "An error occurred during logout."));
        }
    }

    // front에서 refreshToken을 보내서 갱신하려는 경우에.
    // 프론트에서 refreshToken 넘겨주고 갱신하는 부분.
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshRequestDTO> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {

        try {
            // 1. refreshToken 존재 여부 및 유효성 검증
            if (refreshToken == null || !jwtUtil.verifyToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(TokenRefreshRequestDTO.builder()
                                .status(0)  // 상태 0 -> 오류 발생
                                .message("Invalid or missing refresh token.")
                                .build());
            }

            // 2. refreshToken을 통해 사용자 정보 조회 (DB에 있는지 확인)
            User user = refreshTokenService.getUserByRefreshToken(refreshToken);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(TokenRefreshRequestDTO.builder()
                                .status(0)
                                .message("User not found for the provided refresh token.")
                                .build());
            }

            // 3. 유효한 refreshToken이 있다면 새로운 accessToken 발급
            String newAccessToken = jwtUtil.generateAccessToken(user);
            Date expiresAt = jwtUtil.getExpiration(newAccessToken); // 토큰 만료 시간 추출

            // 4. 클라이언트에 accessToken과 만료 시간 응답
            return ResponseEntity.ok(TokenRefreshRequestDTO.builder()
                    .status(1)  // 상태 1 -> 성공
                    .accessToken(newAccessToken)
                    .expiresAt(expiresAt.toInstant().toString()) // ISO 8601 형식
                    .build());

        } catch (Exception e) {
            // 5. 서버 내부 오류 발생 시 500 응답
            log.error("Token refresh error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(TokenRefreshRequestDTO.builder()
                            .status(0)
                            .message("Internal server error during token refresh.")
                            .build());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String token = jwtUtil.extractAccessTokenFromRequest(request);

        if (token == null || !jwtUtil.verifyToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰입니다.");
        }

        String email = jwtUtil.getUid(token);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return ResponseEntity.ok(SignInResponseDTO.builder()
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .credit(user.getCredit())
                .role(user.getRole())
                .isLoggedIn(true)
                .message("로그인 유지 완료")
                .build());
    }
}
