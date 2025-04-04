package com.example.backend.payment;

import com.example.backend.dto.payment.PaymentConfirmRequest;
import com.example.backend.dto.payment.PaymentResultResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<PaymentResultResponse> confirmPayment(@RequestBody PaymentConfirmRequest request,
                                                                @RequestHeader("Authorization") String authHeader) {

        String accessToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;

        try {
            System.out.println("paymentKey: " + request.getPaymentKey());
            System.out.println("orderId: " + request.getOrderId());
            System.out.println("amount: " + request.getAmount());
            System.out.println("accessToken: " + accessToken);

            Map<String, Object> result = paymentService.processPaymentConfirmation(request, accessToken);

            System.out.println("결제 성공. 총 크레딧: " + result.get("credit"));
            return ResponseEntity.ok(new PaymentResultResponse(
                    "결제 완료 및 크레딧 적립 완료!",
                    (int) result.get("credit"),
                    (String) result.get("receiptUrl")
            ));

        } catch (HttpClientErrorException e) {
            // Toss가 이미 처리된 결제라고 응답한 경우 (중복 confirm 방지)
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST &&
                    e.getResponseBodyAsString().contains("ALREADY_PROCESSED_PAYMENT")) {
                System.out.println("⚠️ 이미 처리된 결제 요청입니다. 중복 confirm 생략.");
                return ResponseEntity.ok(new PaymentResultResponse(
                        "이미 처리된 결제입니다.",
                        0,
                        null
                ));
            }

            // Toss 관련 오류
            System.err.println("❌ Toss 결제 확인 실패: " + e.getResponseBodyAsString());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new PaymentResultResponse("Toss 결제 오류: " + e.getMessage(), 0, null)
            );

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(
                    new PaymentResultResponse("결제 실패: " + e.getMessage(), 0, null)
            );
        }
    }
}
