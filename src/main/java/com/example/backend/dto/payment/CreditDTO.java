package com.example.backend.dto.payment;

import lombok.Data;

@Data
public class CreditDTO {
    private String userId;
    private int amount;      // 요청 시: 차감할 금액
    private int balance;     // 응답 시: 남은 크레딧
    private String approvedAt;
}
