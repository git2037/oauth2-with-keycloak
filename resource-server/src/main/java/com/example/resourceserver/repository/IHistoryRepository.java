package com.example.resourceserver.repository;

import com.example.resourceserver.entity.HistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.stream.Stream;

public interface IHistoryRepository extends JpaRepository<HistoryEntity, Long> {
    Stream<HistoryEntity> findAllByUserId(String userId);
}
