package com.example.backend.payment;

import com.example.backend.config.jwt.JwtUtil;
import com.example.backend.dto.payment.PaymentConfirmRequest;
import com.example.backend.dto.payment.TossPaymentResponse;
import com.example.backend.entity.PaymentHistory;
import com.example.backend.entity.User;
import com.example.backend.user.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    public Map<String, Object> processPaymentConfirmation(PaymentConfirmRequest request, String accessToken) throws JsonProcessingException {
        // 1. Toss에 결제 승인 요청
        HttpHeaders headers = new HttpHeaders();
        String encodedKey = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes());
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("paymentKey", request.getPaymentKey());
        body.put("orderId", request.getOrderId());
        body.put("amount", request.getAmount());

        HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://api.tosspayments.com/v1/payments/confirm",
                httpRequest,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Toss 결제 승인 실패");
        }

        // 2. Toss 응답을 DTO로 파싱
        TossPaymentResponse tossResponse = objectMapper.readValue(response.getBody(), TossPaymentResponse.class);

        // 3. 사용자 정보 추출 (JWT)
        String email = jwtUtil.getUid(accessToken); // Bearer 제거 필요)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("사용자 없음"));

        // 4. 크레딧 증가
        int amount = tossResponse.getTotalAmount();
        user.setCredit(user.getCredit() + amount);
        userRepository.save(user);

        // 5. 결제 내역 저장
        PaymentHistory history = PaymentHistory.builder()
                .paymentKey(tossResponse.getPaymentKey())
                .orderId(tossResponse.getOrderId())
                .amount(amount)
                .method(tossResponse.getMethod())
                .receiptUrl(tossResponse.getReceipt().getUrl())
                .approvedAt(ZonedDateTime.parse(tossResponse.getApprovedAt()))
                .user(user)
                .type("CHARGE")
                .build();

        paymentHistoryRepository.save(history);

        return Map.of(
                "credit", user.getCredit(),
                "receiptUrl", tossResponse.getReceipt().getUrl()
        );
    }

    public List<PaymentHistory> getHistoryForUser(User user) {
        return paymentHistoryRepository.findAllByUserOrderByApprovedAtDesc(user);
    }

    public void saveUseHistory(User user, int amount) {
        PaymentHistory history = PaymentHistory.builder()
                .amount(amount)
                .approvedAt(ZonedDateTime.from(LocalDateTime.now()))
                .user(user)
                .type("USE")
                .build();

        paymentHistoryRepository.save(history);
    }
}