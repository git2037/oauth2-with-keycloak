package com.example.ebook.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BookDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String authors;
    private String imagePath;
    private String contentFilePath;
    private String categoryID;
    private String contentFileName;
    private String imageFileName;
}
