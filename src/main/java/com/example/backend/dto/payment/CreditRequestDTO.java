package com.example.backend.dto.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CreditRequestDTO {
    @Schema(description = "차감할 크레딧 금액", example = "1000")
    private int amount;
}
