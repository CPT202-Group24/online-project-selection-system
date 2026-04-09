package com.group24.projectselection.repository;

import com.group24.projectselection.model.ConflictLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConflictLogRepository extends JpaRepository<ConflictLog, Long> {
}
