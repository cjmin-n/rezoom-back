package com.example.backend.payment;

import com.example.backend.config.jwt.JwtUtil;
import com.example.backend.dto.payment.CreditResponseDTO;
import com.example.backend.dto.payment.PaymentConfirmRequest;
import com.example.backend.dto.payment.PaymentResultResponse;
import com.example.backend.entity.PaymentHistory;
import com.example.backend.entity.User;
import com.example.backend.swagger.PaymentControllerDocs;
import com.example.backend.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController implements PaymentControllerDocs {
    private final PaymentService paymentService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    private final int cost = 500;

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
                System.out.println("이미 처리된 결제 요청입니다. 중복 confirm 생략.");
                return ResponseEntity.ok(new PaymentResultResponse(
                        "이미 처리된 결제입니다.",
                        0,
                        null
                ));
            }
            // Toss 관련 오류
            System.err.println("Toss 결제 확인 실패: " + e.getResponseBodyAsString());
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

    // credit 사용
    @PostMapping("/credit")
    public ResponseEntity<CreditResponseDTO> useCredit(@RequestHeader("Authorization") String authHeader) {
        String accessToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        User user = jwtUtil.getUserFromToken(accessToken);

        // 잔액 부족 시
        if (user.getCredit() < cost) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        // user의 credit 필드 수정 (크레딧 차감)
        userService.useCredit(user, cost);

        // paymentHistory 저장 (차감 내역)
        paymentService.saveUseHistory(user, cost);

        CreditResponseDTO response = new CreditResponseDTO();
        response.setUserId(String.valueOf(user.getId()));
        response.setAmount(cost);
        response.setBalance(user.getCredit());
        response.setApprovedAt(LocalDateTime.now());
        response.setType("USE");

        return ResponseEntity.ok(response);
    }

    // 크레딧 롤백
    @PostMapping("/rollback")
    public ResponseEntity<String> rollbackCredit(@RequestHeader("Authorization") String authHeader) {
        String accessToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        User user = jwtUtil.getUserFromToken(accessToken);

        try {
            userService.rollbackCredit(user,cost);
            return ResponseEntity.ok("크레딧 롤백 완료!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("크레딧 롤백 실패");
        }
    }

    // 크레딧 충전/사용 내역
    @GetMapping("/credit")
    public ResponseEntity<Map<String, Object>> getPaymentHistory(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        String accessToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        User user = jwtUtil.getUserFromToken(accessToken);

        List<PaymentHistory> historyList = paymentService.getHistoryForUser(user);

        List<CreditResponseDTO> all = historyList.stream().map(h -> {
                    CreditResponseDTO dto = new CreditResponseDTO();
                    dto.setUserId(String.valueOf(user.getId()));
                    dto.setAmount(h.getAmount());
                    dto.setBalance(user.getCredit());
                    dto.setApprovedAt(h.getApprovedAt());
                    dto.setType(h.getType());
                    return dto;
                }).sorted((a, b) -> b.getApprovedAt().compareTo(a.getApprovedAt()))
                .collect(Collectors.toList());

        // 가입 리워드 항목은 마지막에 추가
        CreditResponseDTO welcome = new CreditResponseDTO();
        welcome.setUserId(String.valueOf(user.getId()));
        welcome.setAmount(1000);
        welcome.setBalance(user.getCredit());
        welcome.setType("WELCOME");
        welcome.setApprovedAt(null);
        all.add(welcome);

        Map<String, Object> result = Map.of(
                "content", all,
                "totalElements", all.size(),
                "totalPages", (int) Math.ceil((double) all.size() / size),
                "page", page
        );

        return ResponseEntity.ok(result);
    }
}
