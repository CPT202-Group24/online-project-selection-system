package com.group24.projectselection.repository;

import com.group24.projectselection.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @EntityGraph(attributePaths = "admin")
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @EntityGraph(attributePaths = "admin")
    @Query("""
            SELECT a FROM AuditLog a JOIN a.admin u
            WHERE LOWER(a.actionType) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(a.entityType) LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))
            """)
    Page<AuditLog> searchByKeyword(@Param("q") String q, Pageable pageable);
}
