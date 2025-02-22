package com.example.resourceserver.service;

import com.example.resourceserver.dto.CategoryDTO;
import com.example.resourceserver.mapper.CategoryMapper;
import com.example.resourceserver.repository.ICategoryRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
public class CategoryService {
    private final ICategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(ICategoryRepository categoryRepository, CategoryMapper categoryMapper) {
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    public Stream<CategoryDTO> findAll(){
        return categoryRepository.findAll().stream().map(categoryMapper::toDto);
    }

    public Optional<CategoryDTO> findById(String id) {
        return categoryRepository.findById(id).map(categoryMapper::toDto);
    }

    public CategoryDTO save(CategoryDTO categoryDTO) {
        return categoryMapper.toDto(
                categoryRepository.save(categoryMapper.toEntity(categoryDTO))
        );
    }

    public CategoryDTO update(CategoryDTO categoryDTO) {
        return categoryMapper.toDto(
                categoryRepository.save(categoryMapper.toEntity(categoryDTO))
        );
    }
}
