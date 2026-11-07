package com.backend.modulo.repository;

import com.backend.modulo.entity.SupportReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportReportRepository extends JpaRepository<SupportReport, Long> {
    List<SupportReport> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<SupportReport> findAllByOrderByCreatedAtDesc();
    List<SupportReport> findByStatusOrderByCreatedAtDesc(SupportReport.Status status);
}
