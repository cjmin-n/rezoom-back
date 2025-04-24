package com.example.backend.pdf;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.example.backend.config.MultipartInputStreamFileResource;
import com.example.backend.config.aws.S3Uploader;
import com.example.backend.dto.*;
import com.example.backend.dto.AgentFeedbackDTO;
import com.example.backend.entity.Pdf;
import com.example.backend.entity.User;
import com.example.backend.user.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
    private static final String fastApiUrl = "https://ai.rezoom.store";
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
        System.out.println(fastApiUrl);
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
        try {
            if (role.equals("APPLICANT")) {
                objectId = sendToPdfUpload(file);
            } else {
                objectId = sendToPdfUpload(file, startDay, endDay);
            }
        } catch (Exception e) {
            amazonS3.deleteObject(s3BucketName, key);
            throw new RuntimeException("FastAPI 업로드 실패 - S3 롤백 완료", e);
        }
        // 5. DB 저장
        try {
            User user = userRepository.findById(userId).get();
            Pdf mapping = Pdf.builder()
                    .user(user)
                    .pdfUri(fileUrl)
                    .pdfFileName(originalFileName)
                    .mongoObjectId(objectId)
                    .uploadedAt(LocalDateTime.now())
                    .build();
            pdfRepository.save(mapping);

        } catch (Exception e) {
            amazonS3.deleteObject(s3BucketName, key);
            deleteFastApiPdf(objectId);
            throw new RuntimeException("RDB 저장 실패 - 전체 롤백 완료", e);
        }
        return "저장 완료";
    }


    private String sendToPdfUpload(MultipartFile file) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getSize()
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    fastApiUrl + "/resumes/upload-pdf", requestEntity, Map.class);

            System.out.println("[✅ FastAPI 응답]: " + response);  // ✅ 응답 로깅
            return response.getBody().get("object_id").toString();

        } catch (Exception e) {
            System.out.println("[❌ FastAPI 요청 실패]");
            e.printStackTrace();  // ✅ 반드시 출력
            throw new RuntimeException("FastAPI 업로드 실패", e);
        }
    }
    private String sendToPdfUpload(MultipartFile file, LocalDate startDay, LocalDate endDay) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new MultipartInputStreamFileResource(
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
        } catch (Exception e) {
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
            objectMapper.registerModule(new JavaTimeModule()); // LocalDate 파싱 지원

            List<PostingResponseDTO> resultList = new ArrayList<>();

            // 루트가 객체이므로 중간 Wrapper 사용
            EvalWrapperResponse wrapper = objectMapper.readValue(
                    response.getBody(),
                    EvalWrapperResponse.class
            );

            String resumeText = wrapper.getResumeText();

            // wrapper 내부 리스트 반복
            for (PostingResultWrapper raw : wrapper.getMatchingResumes()) {
                OneToneDTO result = raw.getResult();  // 이미 매핑된 JSON 객체

                Optional<Pdf> pdfOpt = pdfRepository.findByMongoObjectId(raw.getObjectId());
                String name = pdfOpt.map(pdf -> pdf.getUser().getName()).orElse("알 수 없음");

                String presignedUrl = pdfOpt.map(pdf -> {
                    String key = extractS3KeyFromUrl(pdf.getPdfUri());
                    return s3Uploader.generatePresignedUrl("rezoombucket-v2", key, 30);
                }).orElse(null);

                // 응답 DTO 구성
                PostingResponseDTO dto = new PostingResponseDTO();
                dto.setResumeText(resumeText);
                dto.setTotalScore(result.getTotalScore());
                dto.setResumeScore(result.getResumeScore());
                dto.setSelfintroScore(result.getSelfintroScore());
                dto.setOpinion1(result.getOpinion1());
                dto.setSummary(result.getSummary());
                dto.setEvalResume(result.getEvalResume());
                dto.setEvalSelfintro(result.getEvalSelfintro());

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
            // 1. 파일 → form-data 구성
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("job_posting", new MultipartInputStreamFileResource(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getSize()
            ));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    fastApiUrl + "/postings/match_job_posting",
                    requestEntity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            List<ResumeResponseDTO> resultList = new ArrayList<>();

            System.out.println(response.getBody());
            // ✅ Wrapper 클래스에서 내부 리스트 추출
            EvalWrapperResponse wrapper = objectMapper.readValue(
                    response.getBody(),
                    EvalWrapperResponse.class
            );

            for (ResumeResultWrapper raw : wrapper.getMatchingResume()) {
                OneToneDTO result = raw.getResult();

                Optional<Pdf> pdfOpt = pdfRepository.findByMongoObjectId(raw.getObjectId());
                String name = pdfOpt.map(pdf -> pdf.getUser().getName()).orElse("알 수 없음");
                Optional<LocalDateTime> uploadAt = pdfOpt.map(Pdf::getUploadedAt);
                String presignedUrl = pdfOpt.map(pdf -> {
                    String key = extractS3KeyFromUrl(pdf.getPdfUri());
                    return s3Uploader.generatePresignedUrl("rezoombucket-v2", key, 30);
                }).orElse(null);
                ResumeResponseDTO dto = new ResumeResponseDTO();
                dto.setTotalScore(result.getTotalScore());
                dto.setResumeScore(result.getResumeScore());
                dto.setSelfintroScore(result.getSelfintroScore());
                dto.setOpinion1(result.getOpinion1());
                dto.setSummary(result.getSummary());
                dto.setEvalResume(result.getEvalResume());
                dto.setEvalSelfintro(result.getEvalSelfintro());

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


    public List<OneToneDTO> matchResumeAndPosting(MultipartFile resume, MultipartFile posting) throws IOException {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("resume", new MultipartInputStreamFileResource(resume.getInputStream(), resume.getOriginalFilename(), resume.getSize()));
        body.add("job_posting", new MultipartInputStreamFileResource(posting.getInputStream(), posting.getOriginalFilename(), posting.getSize()));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                fastApiUrl + "/resumes/compare_resume_posting",
                requestEntity,
                String.class
        );
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.readTree(response.getBody());

        // JSON 구조: { "result": { "markup": "...", "data": { ... } } }
        JsonNode resultNode = root.get("result");
        if (resultNode == null || resultNode.isNull()) {
            throw new IllegalStateException("응답에 'result' 필드가 없습니다.");
        }

        JsonNode dataNode = resultNode.get("data");
        if (dataNode == null || dataNode.isNull()) {
            throw new IllegalStateException("응답에 'data' 필드가 없습니다.");
        }

        OneToneDTO dto = objectMapper.treeToValue(dataNode, OneToneDTO.class);

        return List.of(dto);
    }

    public AgentFeedbackDTO analyzeWithAgent(String resumeEval, String selfintroEval, int resumeScore, int selfintroScore, String resumeText) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("resume_eval", resumeEval);
            requestBody.put("selfintro_eval", selfintroEval);
            requestBody.put("resume_score", resumeScore);
            requestBody.put("selfintro_score", selfintroScore);
            requestBody.put("resume_text", resumeText);

            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    fastApiUrl + "/agent/analyze",
                    requestEntity,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode feedbackNode = root.get("agent_feedback");

            return AgentFeedbackDTO.builder()
                    .type(feedbackNode.get("type").asText())
                    .message(feedbackNode.get("message").asText())
                    .gapText(feedbackNode.get("gap_text").asText())
                    .planText(feedbackNode.get("plan_text").asText())
                    .selfIntroFeedback(feedbackNode.get("self_intro_feedback").asText())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("FastAPI 호출 실패: " + e.getMessage());
        }
    }
}