package com.example.resourceserver.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "history")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class HistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private BookEntity bookId;

    @Column(name = "user_id", updatable = false)
    private String userId;
}
