package com.example.backend.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostingMatchResultDTO {
    private String name;
    private String phone;
    private String email;
    private List<String> skills;
    private String education;
    private String experience;
    private String self_intro;
    private double similarity_score;
}
