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
    private String companyName;
    private String businessNumber;
    private String role;

    public User toUser(BCryptPasswordEncoder bCryptPasswordEncoder) {
        String finalName = role.equals("HR") ? companyName : name;
        String finalPhone = role.equals("HR") ? businessNumber : phone;

        return User.builder()
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .name(finalName)
                .phone(finalPhone)
                .age(age)
                .role(role)
                .build();
    }
}

