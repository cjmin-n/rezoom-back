package com.example.backend.token;

import com.example.backend.entity.RefreshToken;
import com.example.backend.entity.User;
import com.example.backend.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;  // DB에서 refreshToken을 관리하는 Repository

    // 사용자별 refreshToken 저장
    public void saveRefreshToken(User user, String refreshToken) {
        RefreshToken newRefreshToken = new RefreshToken();
        newRefreshToken.setUser(user);  // 사용자 설정
        newRefreshToken.setRefreshToken(refreshToken);  // refreshToken 설정
        refreshTokenRepository.save(newRefreshToken);  // 저장
    }

    // 사용자의 refreshToken 조회
    public RefreshToken getRefreshTokenByUser(User user) {
        return refreshTokenRepository.findByUser(user);  // DB에서 해당 사용자와 연결된 refreshToken 조회
    }

    // refreshToken을 사용하여 사용자 조회
    public User getUserByRefreshToken(String refreshToken) {
        RefreshToken refreshTokenObj = refreshTokenRepository.findByRefreshToken(refreshToken);
        return refreshTokenObj != null ? refreshTokenObj.getUser() : null;
    }

    // refreshToken 삭제 (로그아웃 시)
    public void deleteRefreshToken(User user) {
        RefreshToken refreshToken = refreshTokenRepository.findByUser(user);
        if (refreshToken != null) {
            refreshTokenRepository.delete(refreshToken);  // DB에서 삭제
        }
    }
}
