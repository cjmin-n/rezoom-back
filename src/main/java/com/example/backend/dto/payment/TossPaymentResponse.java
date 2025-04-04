package com.example.backend.dto.payment;

import lombok.Data;

@Data
public class TossPaymentResponse {
    private String paymentKey;
    private String orderId;
    private int totalAmount;
    private String method;
    private String approvedAt;
    private Receipt receipt;

    @Data
    public static class Receipt {
        private String url;
    }
}
