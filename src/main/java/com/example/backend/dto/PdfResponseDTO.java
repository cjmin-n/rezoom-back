package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PdfResponseDTO {

    private Long userId;
    private List<PdfInfo> pdfs;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class PdfInfo {
        private Long id;
        private String pdfFileName;
        private String mongoObjectId;
        private LocalDateTime uploadedAt;
    }
}
