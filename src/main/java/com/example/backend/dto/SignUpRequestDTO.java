package com.example.backend.dto;

import com.example.backend.entity.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import lombok.Getter;

@Getter
public class SignUpRequestDTO {
    private String email;
    private String password;
    private String name;
    private String phone;
    private int age;
    private String role;

    public User toUser(BCryptPasswordEncoder bCryptPasswordEncoder){
        return User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .name(name)
                .age(age)
                .phone(phone)
                .role(role)
                .build();
    }

}
