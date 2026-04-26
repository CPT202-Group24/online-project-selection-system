package com.group24.projectselection.repository;

import com.group24.projectselection.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    boolean existsByNameIgnoreCase(String name);

    List<Category> findAllByOrderByNameAsc();

    List<Category> findByIsActiveTrueOrderByNameAsc();
}
