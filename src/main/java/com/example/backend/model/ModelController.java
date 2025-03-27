package com.example.backend.model;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
public class ModelController {

    @Value("${fastapi.base-url}")
    private String fastApiBaseUrl;

    @Value("${fastapi.endpoint}")
    private String fastApiEndpoint;

    @PostMapping("/upload-pdf")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
        try {
            // 파일 전송 준비 (RestTemplate 사용)
            RestTemplate restTemplate = new RestTemplate();
            LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource fileAsResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("file", fileAsResource);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // baseUrl과 endpoint를 조합하여 FastAPI URL 생성
            String url = fastApiBaseUrl + fastApiEndpoint;
            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing PDF: " + e.getMessage());
        }
    }


}
