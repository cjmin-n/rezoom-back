package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentFeedbackDTO {
    private String type;
    private String message;
    private String gapText;
    private String planText;
    private String selfIntroFeedback;
}