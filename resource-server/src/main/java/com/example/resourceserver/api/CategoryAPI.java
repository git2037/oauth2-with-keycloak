package com.example.resourceserver.api;

import com.example.resourceserver.dto.CategoryDTO;
import com.example.resourceserver.dto.ResponseAPI;
import com.example.resourceserver.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryAPI {
    private final CategoryService categoryService;

    public CategoryAPI(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> findAll() {
        return ResponseEntity.ok(
                categoryService.findAll().toList()

        );
    }

    @PostMapping("/categories")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<CategoryDTO> save(@RequestBody CategoryDTO categoryDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        categoryService.save(categoryDTO)
                );
    }

    @PutMapping("/categories/{id}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<CategoryDTO> update(@PathVariable String id, @RequestBody CategoryDTO categoryDTO) {
        return categoryService.findById(id)
                .map(
                        ResponseEntity::ok
                )
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(
                                null
                        )
                );
    }
}
