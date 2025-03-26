package com.example.backend.token;

import com.example.backend.entity.RefreshToken;
import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    RefreshToken findByUser(User user);

    RefreshToken findByRefreshToken(String refreshToken);
}
