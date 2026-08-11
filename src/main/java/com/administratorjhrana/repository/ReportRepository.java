package com.administratorjhrana.repository;

import com.administratorjhrana.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Page<Report> findAllByOrderByUploadedAtDesc(Pageable pageable);

    Page<Report> findByGuardNameContainingIgnoreCase(String guardName, Pageable pageable);

    Page<Report> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE r.date BETWEEN :start AND :end ORDER BY r.uploadedAt DESC")
    Page<Report> findByDateBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, Pageable pageable);

    @Query("SELECT DISTINCT r.guardName FROM Report r WHERE r.guardName IS NOT NULL AND r.guardName != ''")
    List<String> findDistinctGuardNames();

    @Query("SELECT DISTINCT r.title FROM Report r WHERE r.title IS NOT NULL AND r.title != ''")
    List<String> findDistinctTitles();
}
