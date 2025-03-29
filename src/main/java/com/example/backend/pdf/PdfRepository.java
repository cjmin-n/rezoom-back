package com.example.backend.pdf;

import com.example.backend.entity.Pdf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PdfRepository extends JpaRepository<Pdf, Long> {
    List<Pdf> findAllByUserId(Long userId);
}
