package com.example.backend.user;

import com.example.backend.dto.SignUpRequestDTO;
import com.example.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    // 사용자 저장
    public User saveUser(SignUpRequestDTO signUpRequestDTO) {
        User user = signUpRequestDTO.toUser(bCryptPasswordEncoder);

        return userRepository.save(user);
    }

    // 전체 사용자 조회
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 특정 이름의 사용자 조회
    public List<User> getUsersByName(String name) {
        return userRepository.findByName(name);
    }
}
