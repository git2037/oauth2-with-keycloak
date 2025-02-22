package com.example.ebookmanager.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BookDTO {
    private String id;
    private String name;
    private String authors;
    private String imagePath;
    private String contentFilePath;
    private String categoryID;
    private MultipartFile imageFile;
    private MultipartFile contentFile;
    private String contentFileName;
    private String imageFileName;
    private String introduction;
}
