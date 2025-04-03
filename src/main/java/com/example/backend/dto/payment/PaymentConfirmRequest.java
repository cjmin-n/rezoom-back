package com.example.backend.dto.payment;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private int amount;
}

