package com.example.backend.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RawResultWrapper {
    private String result;
    private String objectId;
    private LocalDate startDay;
    private LocalDate endDay;
}