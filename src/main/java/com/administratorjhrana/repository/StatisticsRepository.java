package com.administratorjhrana.repository;

import com.administratorjhrana.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatisticsRepository extends JpaRepository<Report, Long> {

    @Query("SELECT v.type, COUNT(v) FROM Violation v " +
            "WHERE (:from IS NULL OR v.detectedAt >= :from) AND (:to IS NULL OR v.detectedAt <= :to) " +
            "GROUP BY v.type")
    List<Object[]> countViolationsByType(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT cl.checkpointName, AVG(EXTRACT(EPOCH FROM cl.timestamp)) " +
            "FROM CheckpointLog cl " +
            "WHERE (:from IS NULL OR cl.timestamp >= :from) AND (:to IS NULL OR cl.timestamp <= :to) " +
            "GROUP BY cl.checkpointName")
    List<Object[]> getCheckpointPassingTimes(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
