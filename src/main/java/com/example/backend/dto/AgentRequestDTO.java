package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentRequestDTO {
    private String resume_eval;
    private String selfintro_eval;
    private int resume_score;
    private int selfintro_score;

    @JsonProperty("resume_text")
    private String selfIntroFeedback;
}