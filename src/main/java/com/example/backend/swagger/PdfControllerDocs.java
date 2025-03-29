package com.example.backend.swagger;

import com.example.backend.dto.PdfResponseDTO;
import com.example.backend.dto.SecurityUserDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "PDF", description = "PDF 업로드 및 조회 API")
public interface PdfControllerDocs {

    @Operation(
            summary = "PDF 업로드",
            description = "사용자가 PDF 파일을 업로드합니다. (토큰 인증 필요)",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(mediaType = "multipart/form-data",
                            schema = @Schema(type = "object", format = "binary", implementation = MultipartFile.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "업로드 성공",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "400", description = "PDF 파일 형식이 아님",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "500", description = "서버 오류",
                            content = @Content(schema = @Schema(implementation = String.class)))
            }
    )
    ResponseEntity<String> uploadSinglePdf(
            @Parameter(description = "업로드할 PDF 파일", required = true)
            MultipartFile file,
            @Parameter(hidden = true) SecurityUserDto authenticatedUser
    );

    @Operation(
            summary = "PDF 리스트 조회",
            description = "인증된 사용자의 PDF 업로드 목록을 반환합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = PdfResponseDTO.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
            }
    )
    ResponseEntity<PdfResponseDTO> getPdf(
            @Parameter(hidden = true) SecurityUserDto authenticatedUser
    );
}
