package com.group24.projectselection.repository;

import com.group24.projectselection.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    List<Application> findByStudentId(Long studentId);

    List<Application> findByProjectId(Long projectId);

    List<Application> findByStatus(Application.ApplicationStatus status);

    @Query("""
            SELECT a.student.id FROM Application a
            WHERE a.status = 'accepted'
            GROUP BY a.student.id
            HAVING COUNT(a) >= 2
            """)
    List<Long> findStudentIdsWithMultipleAcceptedApplications();
}
