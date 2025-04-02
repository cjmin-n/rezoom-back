package com.example.backend.swagger;

import com.example.backend.dto.PdfResponseDTO;
import com.example.backend.dto.PostingMatchResultDTO;
import com.example.backend.dto.ResumeMatchResultDTO;
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

import java.util.List;
import java.util.Map;

@Tag(name = "PDF", description = "PDF 관련 API")
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
                    @ApiResponse(responseCode = "400", description = "PDF 파일 형식이 아님"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "500", description = "서버 오류")
            }
    )
    ResponseEntity<String> uploadSinglePdf(
            @Parameter(description = "업로드할 PDF 파일", required = true)
            MultipartFile file,
            @Parameter(hidden = true) SecurityUserDto authenticatedUser
    );

    @Operation(
            summary = "PDF 삭제",
            description = "PDF ID를 통해 특정 PDF를 삭제합니다. (토큰 인증 필요)",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(
                            description = "삭제할 PDF의 ID",
                            example = "{\"pdfId\": 1}"
                    ))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "400", description = "요청 본문 오류 또는 PDF ID 없음"),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
            }
    )
    ResponseEntity<String> deletePdfById(
            @RequestBody Map<String, Long> request,
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

    @Operation(
            summary = "이력서 → 채용공고 매칭",
            description = "업로드된 이력서를 기준으로 적합한 채용공고를 추천합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매칭 성공",
                            content = @Content(schema = @Schema(implementation = ResumeMatchResultDTO.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
            }
    )
    ResponseEntity<List<ResumeMatchResultDTO>> resume2posting(
            @Parameter(description = "이력서 PDF 파일", required = true)
            MultipartFile file,
            @Parameter(hidden = true) SecurityUserDto authenticatedUser
    );

    @Operation(
            summary = "채용공고 → 이력서 매칭",
            description = "업로드된 채용공고를 기준으로 적합한 이력서를 추천합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매칭 성공",
                            content = @Content(schema = @Schema(implementation = PostingMatchResultDTO.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
            }
    )
    ResponseEntity<List<PostingMatchResultDTO>> matchJobPosting(
            @Parameter(description = "채용공고 PDF 파일", required = true)
            MultipartFile file,
            @Parameter(hidden = true) SecurityUserDto user
    );

    @Operation(
            summary = "이력서 + 채용공고 동시 업로드 후 매칭",
            description = "이력서와 채용공고 PDF를 동시에 업로드하고 매칭 결과를 확인합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "매칭 성공",
                            content = @Content(schema = @Schema(implementation = String.class))),
                    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
                    @ApiResponse(responseCode = "500", description = "매칭 실패")
            }
    )
    ResponseEntity<String> uploadMultipleFiles(
            @Parameter(hidden = true) SecurityUserDto user,
            @Parameter(description = "이력서 PDF 파일", required = true)
            MultipartFile file1,
            @Parameter(description = "채용공고 PDF 파일", required = true)
            MultipartFile file2
    );
}
