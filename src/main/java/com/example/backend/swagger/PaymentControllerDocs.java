package com.example.backend.swagger;

import com.example.backend.dto.payment.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Payment", description = "결제 및 크레딧 관련 API")
public interface PaymentControllerDocs {

    @Operation(
            summary = "결제 승인 및 크레딧 적립",
            description = "Toss 결제 완료 후 해당 정보를 바탕으로 사용자의 크레딧을 적립합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentConfirmRequest.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "결제 성공 및 크레딧 적립 완료",
                            content = @Content(schema = @Schema(implementation = PaymentResultResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Toss 결제 오류 또는 중복 요청",
                            content = @Content(schema = @Schema(implementation = PaymentResultResponse.class))),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(schema = @Schema(implementation = PaymentResultResponse.class)))
            }
    )
    ResponseEntity<PaymentResultResponse> confirmPayment(
            @RequestBody PaymentConfirmRequest request,
            @Parameter(description = "Bearer 액세스 토큰", required = true, example = "Bearer eyJhbGciOi...") String authHeader
    );

    @Operation(
            summary = "크레딧 사용",
            description = "사용자의 크레딧을 차감합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "크레딧 차감 성공",
                            content = @Content(schema = @Schema(implementation = CreditResponseDTO.class))),
                    @ApiResponse(responseCode = "400", description = "잔액 부족 또는 잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ResponseEntity<CreditResponseDTO> useCredit(
            @Parameter(description = "Bearer 액세스 토큰", required = true, example = "Bearer eyJhbGciOi...") String authHeader
    );
}
