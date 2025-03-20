package com.example.backend.entity.user;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.annotation.Id;

import java.util.List;


@Document(collection = "users")  // users 컬렉션과 매핑
public class User {
    @Id
    private String id;
    private String name;
    private String email;
    private int age;
    private List<String> skills;

    // 기본 생성자
    public User() {}

    // 모든 필드를 포함하는 생성자
    public User(String id, String name, String email, int age, List<String> skills) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
        this.skills = skills;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }
}