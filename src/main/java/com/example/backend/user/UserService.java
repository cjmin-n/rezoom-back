package com.example.backend.user;

import com.example.backend.entity.user.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    // 사용자 저장
    public User saveUser(User user) {
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
