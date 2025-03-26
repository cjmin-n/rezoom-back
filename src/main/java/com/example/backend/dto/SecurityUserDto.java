package com.example.backend.dto;

import com.example.backend.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SecurityUserDto {
    private Long id;
    private String email;
    private String phone;
    private String role;
    private int age;
    private String name;

    public User toUser(){
        return User.builder()
                .email(email)
                .phone(phone)
                .role(role)
                .age(age)
                .name(name)
                .build();

    }

}
