package com.example.resourceserver.dto;

import lombok.*;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CategoryDTO implements Serializable {
    private String id;
    private String name;
    private String description;
}
