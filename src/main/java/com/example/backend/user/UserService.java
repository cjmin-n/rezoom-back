package com.example.backend.user;

import com.example.backend.dto.SignUpRequestDTO;
import com.example.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 사용자 저장
    public User saveUser(SignUpRequestDTO signUpRequestDTO) {
        User user = signUpRequestDTO.toUser(bCryptPasswordEncoder);

        // 튜토리얼 진행하지 않은 상태로 회원가입
        user.setTutorial(false);

        return userRepository.save(user);
    }

    public void tutorialComplete(User authenticatedUser) {
        // 튜토리얼 완료 상태 업데이트
        authenticatedUser.setTutorial(true);
        userRepository.save(authenticatedUser);
    }

    // 전체 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 특정 이름의 사용자 조회
    public List<User> getUsersByName(String name) {
        return userRepository.findByName(name);
    }

    public Optional<User> findByEmail(String username) {
        return userRepository.findByEmail(username);
    }
}
