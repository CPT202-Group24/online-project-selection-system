package com.group24.projectselection.repository;

import com.group24.projectselection.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findAllByOrderByCreatedAtDesc();

    @Query("""
            SELECT u FROM User u
            WHERE u.role = 'student'
            AND u.status = 'active'
            AND NOT EXISTS (
                SELECT a FROM Application a
                WHERE a.student = u
                AND a.status = 'accepted'
            )
            ORDER BY u.name ASC
            """)
    List<User> findActiveStudentsWithNoAcceptedApplication();
}
