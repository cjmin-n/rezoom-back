package com.example.backend.pdf;

import com.example.backend.config.MultipartInputStreamFileResource;
import com.example.backend.dto.PdfResponseDTO;
import com.example.backend.dto.PostingMatchResultDTO;
import com.example.backend.dto.ResumeMatchResultDTO;
import com.example.backend.entity.Pdf;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfService {

    private static final String UPLOAD_DIR = "uploads/";
    private static final String fastApiUrl = "http://localhost:8000";
    @Autowired
    private final PdfRepository pdfRepository;
    private final RestTemplate restTemplate;

    public String handlePdfUpload(MultipartFile file, Long userId) throws IOException {
        // 1. 확장자 체크
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
        }
        // 2. 디렉토리 없으면 생성
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        // 3. 실제 파일명으로 저장 (중복 방지하려면 userId 또는 timestamp 붙이기)
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uuidFileName = UUID.randomUUID().toString() + extension;
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        File pdfFile = new File(uploadPath + File.separator + uuidFileName);

        System.out.println("파일 저장 경로: " + pdfFile.getAbsolutePath());
        file.transferTo(pdfFile); // 여기서 실제 저장 완료됨

        // 5. FastAPI로 전송 → ObjectId 받아오기
        String objectId = sendToPdfUpload(pdfFile);
        System.out.println(objectId);

        // 6. DB 저장
        try {
            Pdf mapping = Pdf.builder()
                    .userId(userId)
                    .pdfUri("/files/" + uuidFileName)  // 실제 접근 경로로 설정
                    .pdfFileName(originalFileName)
                    .mongoObjectId(objectId)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            pdfRepository.save(mapping);

        } catch (Exception e) {
            System.err.println("SQL 저장 실패: " + e.getMessage());
            deleteFastApiPdf(objectId);
            if (pdfFile.exists()) {
                pdfFile.delete();
                System.out.println("로컬 파일 삭제 완료");
            }
            throw e;
        }

        return "저장 완료";
    }


    private String sendToPdfUpload(File pdfFile) {
        System.out.println("요청 URL: " + fastApiUrl);

        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("resume", new FileSystemResource(pdfFile));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(fastApiUrl+"/resumes/upload-pdf", requestEntity, Map.class);

            System.out.println("FastAPI 응답: " + response);
            return response.getBody().get("object_id").toString();

        } catch (Exception e) {
            System.err.println("FastAPI 요청 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("FastAPI 업로드 실패", e);
        }
    }
    public void deleteFastApiPdf(String objectId) {
        try {
            restTemplate.delete(fastApiUrl+"/resumes/delete_resume/"+objectId);
            System.out.println("FastAPI에 PDF 삭제 요청 완료 (ObjectId: " + objectId + ")");
        } catch (Exception e) {
            System.err.println("FastAPI 삭제 요청 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("FastAPI에서 PDF 삭제 실패", e);
        }
    }


    public PdfResponseDTO getUserPdfs(Long userId) {

        // TODO: 페이징처리 해줄거라면, findAllByUserId(userId, pageable)로 해줘야됨.
        List<Pdf> pdfList = pdfRepository.findAllByUserId(userId);

        List<PdfResponseDTO.PdfInfo> pdfInfos = pdfList.stream()
                .map(pdf -> new PdfResponseDTO.PdfInfo(
                        pdf.getId(),
                        pdf.getPdfFileName(),
                        pdf.getMongoObjectId(),
                        pdf.getUploadedAt()
                ))
                .collect(Collectors.toList());

        return new PdfResponseDTO(userId, pdfInfos);
    }

    public String deletePdfById(Long pdfId, Long userId) {

        Pdf pdf = pdfRepository.findById(pdfId)
                .orElseThrow(() -> new IllegalArgumentException("해당 PDF가 존재하지 않습니다."));

        boolean mongoDeleted = false;
        boolean fileDeleted = false;
        boolean sqlDeleted = false;

        // 1. MongoDB 삭제
        try {
            deleteFastApiPdf(pdf.getMongoObjectId());
            mongoDeleted = true;
        } catch (Exception e) {
            System.err.println("MongoDB 삭제 실패: " + e.getMessage());
        }

        // 2. 로컬 파일 삭제
        String uploadPath = System.getProperty("user.dir") + "/uploads/";
        File file = new File(uploadPath + File.separator + extractFileNameFromUri(pdf.getPdfUri()));
        if (file.exists()) {
            fileDeleted = file.delete();
        }

        // 3. SQL 삭제
        try {
            pdfRepository.deleteById(pdfId);
            sqlDeleted = true;
        } catch (Exception e) {
            System.err.println("SQL 삭제 실패: " + e.getMessage());
        }
        return "삭제 완료 \nMongoDB: " + mongoDeleted + "\nFile: " + fileDeleted + "\nSQL: " + sqlDeleted;
    }
    private String extractFileNameFromUri(String uri) {
        return uri.substring(uri.lastIndexOf("/") + 1);
    }

    public List<ResumeMatchResultDTO> resume2posting(MultipartFile file) {
        try {
            // 1. form-data 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("resume", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 2. FastAPI 호출 (JSON 문자열 응답)
            ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl + "/resumes/match_resume", requestEntity, String.class);

            // 3. JSON 파싱: matching_jobs만 추출
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode jobsNode = root.get("matching_jobs");

            // 4. matching_jobs → DTO 리스트로 매핑
            return objectMapper.readValue(
                    jobsNode.toString(),
                    new TypeReference<List<ResumeMatchResultDTO>>() {}
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // 실패 시 빈 리스트 반환
        }
    }

    public List<PostingMatchResultDTO> posting2resume(MultipartFile file) {
        try {
            // 1. 파일 → form-data로 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("job_posting", new MultipartInputStreamFileResource(file.getInputStream(), file.getOriginalFilename()));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 2. FastAPI 호출
            ResponseEntity<String> response = restTemplate.postForEntity(
                    fastApiUrl + "/resumes/match_job_posting", requestEntity, String.class);

            // 3. matching_resumes 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode resumeList = root.get("matching_resumes");

            return objectMapper.readValue(
                    resumeList.toString(),
                    new TypeReference<List<PostingMatchResultDTO>>() {}
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


}


