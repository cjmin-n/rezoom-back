package com.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class PostingResultWrapper {
    private String result;
    private String objectId;
    private LocalDate startDay;
    private LocalDate endDay;
}