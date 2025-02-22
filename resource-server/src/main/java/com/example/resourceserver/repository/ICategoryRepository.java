package com.example.resourceserver.repository;

import com.example.resourceserver.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ICategoryRepository extends JpaRepository<CategoryEntity, String> {
}
