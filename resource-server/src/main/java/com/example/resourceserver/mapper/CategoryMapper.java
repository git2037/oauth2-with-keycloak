package com.example.resourceserver.mapper;

import com.example.resourceserver.dto.CategoryDTO;
import com.example.resourceserver.entity.CategoryEntity;
import lombok.*;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Builder
@Component
public class CategoryMapper {
    public CategoryEntity toEntity(CategoryDTO dto) {
        return CategoryEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .description(dto.getDescription())
                .build();
    }

    public CategoryDTO toDto(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .build();
    }
}
