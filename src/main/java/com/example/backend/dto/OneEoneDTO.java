package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OneEoneDTO {
    private String summary;
    private double total_score;
    private String gpt_answer;
}
