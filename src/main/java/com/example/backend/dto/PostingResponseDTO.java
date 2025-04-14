package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostingResponseDTO {
    private int total_score;
    private int resume_score;
    private int selfintro_score;
    private String opinion1;
    private String summary;
    private String eval_resume;
    private String eval_selfintro;
    private String name;
    private LocalDate startDay;
    private LocalDate endDay;
}