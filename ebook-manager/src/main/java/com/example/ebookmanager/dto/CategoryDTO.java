package com.example.ebookmanager.dto;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CategoryDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    private String id;
    private String name;
    private String description;
}
