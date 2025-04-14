package com.example.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResumeResponseDTO {
    private int total_score;
    private int resume_score;
    private int selfintro_score;
    private String opinion1;
    private String summary;
    private String eval_resume;
    private String eval_selfintro;
    private String name;
    private String uri;
    private LocalDateTime created_at;
}