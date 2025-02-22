package com.example.resourceserver.mapper;

import com.example.resourceserver.dto.BookDTO;
import com.example.resourceserver.entity.BookEntity;
import com.example.resourceserver.util.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    @Value("${location.image}")
    private String imageLocation;

    @Value("${location.content}")
    private String contentLocation;

    public BookDTO toDTO(BookEntity entity) {
        return BookDTO.builder()
                .id(entity.getId())
                .imagePath(FileUtils.getPathFromName(entity.getImageName(), imageLocation))
                .name(entity.getName())
                .authors(entity.getAuthors())
                .contentFilePath(FileUtils.getPathFromName(entity.getContentFileName(), contentLocation))
                .contentFileName(entity.getContentFileName())
                .imageFileName(entity.getImageName())
                .introduction(entity.getIntroduction())
                .build();
    }

    public BookEntity toEntity(BookDTO dto) {
        return BookEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .authors(dto.getAuthors())
                .imageName(FileUtils.getNameFromPath(dto.getImagePath()))
                .contentFileName(FileUtils.getNameFromPath(dto.getContentFilePath()))
                .introduction(dto.getIntroduction())
                .build();
    }
}
