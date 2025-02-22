package com.example.resourceserver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    private String name;

    private String authors;

    private String imageName;

    private String contentFileName;

    @Column(columnDefinition = "text")
    private String introduction;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
}
