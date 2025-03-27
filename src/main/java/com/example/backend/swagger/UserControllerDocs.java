package com.example.backend.swagger;

import com.example.backend.dto.SignUpRequestDTO;
import com.example.backend.dto.UrlResponseDTO;
import com.example.backend.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "User", description = "회원 관련 API")
public interface UserControllerDocs {

    @Operation(
            summary = "회원가입",
            description = "사용자가 회원가입을 진행합니다.",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = SignUpRequestDTO.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 성공",
                            content = @Content(schema = @Schema(implementation = UrlResponseDTO.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "요청 형식 오류"),
                    @ApiResponse(responseCode = "500", description = "서버 에러")
            }
    )
    ResponseEntity<UrlResponseDTO> signup(@RequestBody SignUpRequestDTO signUpRequestDTO);

    @Operation(summary = "모든 사용자 조회", description = "전체 사용자 목록을 반환합니다.")
    ResponseEntity<List<User>> getAllUsers();

    @Operation(summary = "이름으로 사용자 조회", description = "이름으로 사용자 검색")
    ResponseEntity<List<User>> getUserByName(@PathVariable String name);

    @Operation(summary = "에러 테스트", description = "500 에러 테스트용 API")
    String testError();
}