package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.File;
import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_file_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // 예: 유저 식별자

    private String pdfUri;

    @Column(name = "pdf_file_name")
    private String pdfFileName; // 저장된 파일 이름

    @Column(name = "mongo_object_id")
    private String mongoObjectId; // FastAPI → MongoDB 저장 후 받은 ObjectId

    @Column
    private LocalDateTime uploadedAt;
}

