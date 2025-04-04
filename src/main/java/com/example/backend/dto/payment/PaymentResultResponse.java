package com.example.backend.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResultResponse {
    private String message;
    private int credit;
    private String receiptUrl;
}
