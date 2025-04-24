package com.example.backend.token;

import com.example.backend.config.jwt.JwtProperties;
import com.example.backend.dto.StatusResponseDTO;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class TokenProviderService {

    private final JwtProperties jwtProperties;

    public StatusResponseDTO validToken(String token) {
        int result = validTokenStatus(token);
        return StatusResponseDTO.builder()
                .status(result)
                .build();
    }

    public int validTokenStatus(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey( getSecretKey() ) // SecretKey 객체 사용
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return 1;
        } catch (ExpiredJwtException e) {
            return 2;
        } catch (Exception e) {
            return 3;
        }
    }

    private SecretKey getSecretKey() {
        String secret = jwtProperties.getSecret();
        return Keys.hmacShaKeyFor(secret.getBytes());  // Base64 디코딩하지 않고 그대로 사용
    }
}
