package com.example.backend.pdf;

import com.example.backend.entity.Pdf;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfService {

    private static final String UPLOAD_DIR = "uploads/";
    @Autowired
    private final PdfRepository pdfRepository;
    private final RestTemplate restTemplate;

    public String handlePdfUpload(MultipartFile file,Long userId) throws IOException {
        // 1. 확장자 체크
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
        }

        // 2. 디렉토리 없으면 생성
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        // 3. UUID 기반 저장 파일명 생성
        String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        String uuidFileName = UUID.randomUUID() + extension;
        File pdfFile = new File(UPLOAD_DIR + uuidFileName);

        // 4. 파일 저장
        file.transferTo(pdfFile);

        // 5. FastAPI로 전송 → ObjectId 받아오기
        String objectId = sendToFastApi(pdfFile);

        // 6. DB 저장
        Pdf mapping = Pdf.builder()
                .userId(userId) // TODO: 실제 유저 ID로 대체
                .pdfFileName(uuidFileName)
                .mongoObjectId(objectId)
                .build();

        pdfRepository.save(mapping);

        return "저장 완료";
    }

    private String sendToFastApi(File pdfFile) {
        String fastApiUrl = "http://localhost:8000/upload-pdf";

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(pdfFile));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl, requestEntity, Map.class);

        return response.getBody().get("object_id").toString();
    }
}


