package com.administratorjhrana.repository;

import com.administratorjhrana.model.CheckpointLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CheckpointLogRepository extends JpaRepository<CheckpointLog, Long> {
}