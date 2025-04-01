package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeMatchResultDTO {
    private String _id;
    private String title;
    private String description;
    private double similarity_score;
    private String gpt_evaluation;
}