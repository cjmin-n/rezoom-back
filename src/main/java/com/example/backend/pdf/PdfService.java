package com.example.backend.pdf;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.backend.config.MultipartInputStreamFileResource;
import com.example.backend.config.aws.S3Uploader;
import com.example.backend.dto.*;
import com.example.backend.entity.Pdf;
import com.example.backend.entity.User;
import com.example.backend.user.UserRepository;
import com.example.backend.utiles.MarkupChange;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PdfService {
    private final S3Uploader s3Uploader;
    private static final String fastApiUrl = "http://localhost:8000";
    @Autowired
    private final PdfRepository pdfRepository;
    @Autowired
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    @Autowired
    private AmazonS3 amazonS3;

    private final String s3BucketName = "rezoombucket-v2";

    public String handlePdfUpload(MultipartFile file, Long userId, String role, LocalDate startDay,LocalDate endDay) throws IOException {
        // 1. 확장자 체크
        if (!file.getOriginalFilename().toLowerCase().endsWith(".pdf")) {
            throw new IllegalArgumentException("PDF 파일만 업로드 가능합니다.");
        }

        // 2. 파일 이름 생성
        String originalFileName = file.getOriginalFilename();
        String extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uuidFileName = UUID.randomUUID().toString() + extension;

        String basePath = role.equals("APPLICANT") ? "resumes/" : "posting/";
        String key = basePath + uuidFileName;
        // 3. S3 업로드
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType("application/pdf");
        metadata.setContentLength(file.getSize());
        amazonS3.putObject(s3BucketName, key, file.getInputStream(), metadata);
        String fileUrl = amazonS3.getUrl(s3BucketName, key).toString();
        String objectId;
        if (role.equals("APPLICANT")) {
            objectId = sendToPdfUpload(file);
        } else {
            objectId = sendToPdfUpload(file, startDay, endDay);
        }
        // 5. DB 저장
        User user = userRepository.findById(userId).get(); // 조인컬럼이라 이래함 질문 안받슴다
        Pdf mapping = Pdf.builder()
                .user(user)
                .pdfUri(fileUrl)
                .pdfFileName(originalFileName)
                .mongoObjectId(objectId)
                .uploadedAt(LocalDateTime.now())
                .build();

        pdfRepository.save(mapping);
        return "저장 완료";
    }


    private String sendToPdfUpload(MultipartFile file) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("resume", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getSize()
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    fastApiUrl + "/resumes/upload-pdf", requestEntity, Map.class);

            return response.getBody().get("object_id").toString();

        } catch (Exception e) {
            throw new RuntimeException("FastAPI 업로드 실패", e);
        }
    }
    private String sendToPdfUpload(MultipartFile file, LocalDate startDay, LocalDate endDay) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("resume", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getSize()
            ));

            if (startDay != null) body.add("start_day", startDay.toString()); // "2025-04-14"
            if (endDay != null) body.add("end_day", endDay.toString());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    fastApiUrl + "/resumes/upload-pdf", requestEntity, Map.class);

            return response.getBody().get("object_id").toString();

        } catch (Exception e) {
            throw new RuntimeException("FastAPI 업로드 실패 (채용공고)", e);
        }
    }


    public void deleteFastApiPdf(String objectId) {
        try {
            restTemplate.delete(fastApiUrl + "/resumes/delete_resume/" + objectId);
            System.out.println("FastAPI에 PDF 삭제 요청 완료 (ObjectId: " + objectId + ")");
        } catch (Exception e) {
            System.err.println("FastAPI 삭제 요청 실패: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("FastAPI에서 PDF 삭제 실패", e);
        }
    }


    public PdfResponseDTO getUserPdfs(Long userId) {
        List<Pdf> pdfList = pdfRepository.findAllByUserId(userId);

        List<PdfResponseDTO.PdfInfo> pdfInfos = pdfList.stream()
                .map(pdf -> {
                    String presignedUrl = Optional.ofNullable(pdf)
                            .map(p -> {
                                String key = extractS3KeyFromUrl(p.getPdfUri());
                                return s3Uploader.generatePresignedUrl("rezoombucket-v2", key, 30);
                            }).orElse(null);

                    // 빌더 패턴을 사용하여 PdfInfo 객체 생성
                    return PdfResponseDTO.PdfInfo.builder()
                            .id(pdf.getId())
                            .pdfFileName(pdf.getPdfFileName())
                            .mongoObjectId(pdf.getMongoObjectId())
                            .uploadedAt(pdf.getUploadedAt())
                            .presignedUrl(presignedUrl)  // presignedUrl 추가
                            .build();
                })
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
    public String extractS3KeyFromUrl(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        if (idx == -1) return null;
        return url.substring(idx + ".amazonaws.com/".length());
    }

    public List<PostingResponseDTO> resume2posting(MultipartFile file) {
        try {
            // 1. form-data 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("resume", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getSize() // 꼭 필요!
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    fastApiUrl + "/resumes/match_resume",
                    requestEntity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            List<PostingResponseDTO> resultList = new ArrayList<>();

            // 1. 전체 응답을 RawResultWrapper[]로 파싱
            PostingResultWrapper[] rawArray = objectMapper.readValue(
                    response.getBody(),
                    PostingResultWrapper[].class
            );

            // 2. 각 XML 마크업 → Map → PostingResponseDTO로 변환
            for (PostingResultWrapper raw : rawArray) {
                Map<String, Object> parsedXml = MarkupChange.parseXmlResult(raw.getResult());
                Optional<Pdf> pdfOpt = pdfRepository.findByMongoObjectId(raw.getObjectId());
                String name = pdfOpt.map(pdf -> pdf.getUser().getName()).orElse("알 수 없음");
                String presignedUrl = pdfOpt.map(pdf -> {
                    String key = extractS3KeyFromUrl(pdf.getPdfUri());
                    return s3Uploader.generatePresignedUrl("rezoombucket-v2", key, 30);
                }).orElse(null);

                PostingResponseDTO dto = objectMapper.convertValue(parsedXml, PostingResponseDTO.class);
                dto.setStartDay(raw.getStartDay());
                dto.setEndDay(raw.getEndDay());
                dto.setName(name);
                dto.setUri(presignedUrl);
                resultList.add(dto);
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList(); // 실패 시 빈 리스트 반환
        }
    }

    public List<ResumeResponseDTO> posting2resume(MultipartFile file) {
        try {
            // 1. 파일 → form-data로 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("job_posting", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getSize() // 꼭 필요!
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // 2. FastAPI 호출
            ResponseEntity<String> response = restTemplate.postForEntity(
                    fastApiUrl + "/postings/match_job_posting", requestEntity, String.class);

            // 3. matching_resumes 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            List<ResumeResponseDTO> resultList = new ArrayList<>();

            ResumeResultWrapper[] rawArray = objectMapper.readValue(
                    response.getBody(),
                    ResumeResultWrapper[].class
            );

            for (ResumeResultWrapper raw : rawArray) {
                Map<String, Object> parsedXml = MarkupChange.parseXmlResult(raw.getResult());

                Optional<Pdf> pdfOpt = pdfRepository.findByMongoObjectId(raw.getObjectId());

                String name = pdfOpt.map(pdf -> pdf.getUser().getName()).orElse("알 수 없음");
                Optional<LocalDateTime> uploadAt = pdfOpt.map(pdf-> pdf.getUploadedAt());
                String presignedUrl = pdfOpt.map(pdf -> {
                    String key = extractS3KeyFromUrl(pdf.getPdfUri());
                    return s3Uploader.generatePresignedUrl("rezoombucket-v2", key, 30);
                }).orElse(null);

                ResumeResponseDTO dto = objectMapper.convertValue(parsedXml, ResumeResponseDTO.class);
                dto.setName(name);
                dto.setUri(presignedUrl);
                dto.setCreated_at(uploadAt.orElse(null));
                resultList.add(dto);
            }
            return resultList;
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    public List<OneEoneDTO> matchResumeAndPosting(MultipartFile resume, MultipartFile posting) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("resume", new MultipartInputStreamFileResource(resume.getInputStream(), resume.getOriginalFilename(), resume.getSize()));
        body.add("job_posting", new MultipartInputStreamFileResource(posting.getInputStream(), posting.getOriginalFilename(), posting.getSize()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(fastApiUrl + "/resumes/compare_resume_posting", requestEntity, String.class);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode json = objectMapper.readTree(response.getBody());
        System.out.println("📦 FastAPI 응답 JSON: " + json.toPrettyString());

        // DTO로 변환, FastAPI 응답 Null 방어
        OneEoneDTO dto = new OneEoneDTO();
        dto.setTotal_score(json.has("total_score") ? json.get("total_score").asDouble() : 0.0);
        dto.setSummary(json.has("summary") ? json.get("summary").asText() : "");
        dto.setGpt_answer(json.has("gpt_answer") ? json.get("gpt_answer").asText() : "");

        List<OneEoneDTO> result = new ArrayList<>();
        result.add(dto);

        return result;
    }

    public String analyzeWithAgent(String evaluationResult) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("evaluation_result", evaluationResult);

            HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    fastApiUrl + "/agent/analyze",
                    requestEntity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode json = objectMapper.readTree(response.getBody());

            return json.get("agent_feedback").asText();

        } catch (Exception e) {
            throw new RuntimeException("FastAPI 호출 실패: " + e.getMessage());
        }
    }
}