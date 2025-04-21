package com.example.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class EvalWrapperResponse {
    @JsonProperty("matching_resumes")
    private List<PostingResultWrapper> matchingResumes;
    @JsonProperty("matching_resume")
    private List<ResumeResultWrapper> matchingResume;
}