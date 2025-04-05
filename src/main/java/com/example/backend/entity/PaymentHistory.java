package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.ZonedDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String paymentKey;

    private String orderId;

    private int amount;

    private String method;

    private String type; // "CHARGE" 또는 "USE"

    private String receiptUrl;

    private ZonedDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

}
