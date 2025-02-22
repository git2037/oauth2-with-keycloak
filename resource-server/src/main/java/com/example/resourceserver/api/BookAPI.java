package com.example.resourceserver.api;

import com.example.resourceserver.dto.BookDTO;
import com.example.resourceserver.dto.ResponseAPI;
import com.example.resourceserver.service.BookService;
import com.example.resourceserver.util.APIUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api")
public class BookAPI {

    @Value("${location.image}")
    private String imageLocation;

    @Value("${location.content}")
    private String contentLocation;

    private static final int PAGE_SIZE = 8;
    private final BookService bookService;

    public BookAPI(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/books")
    public ResponseEntity<List<BookDTO>> findAll(@RequestParam("page") Optional<Integer> pageParam,
                                                 @RequestParam("search") Optional<String> searchParam) {
        Pageable pageable = pageParam
                .map(integer -> PageRequest.of(integer - 1, PAGE_SIZE))
                .orElseGet(() -> PageRequest.of(0, PAGE_SIZE));

        List<BookDTO> bookDTOList = searchParam
                .map(
                        s -> bookService.findAllByNameContainingIgnoreCase(s, pageable).toList()
                )
                .orElseGet(
                        () -> bookService.findAll(pageable).toList()
                );

        return ResponseEntity.ok(
                bookDTOList
        );
    }

    @GetMapping("/books/count")
    public ResponseEntity<Long> count() {
        return ResponseEntity.ok(bookService.count());
    }

    @GetMapping("/categories/{id}/books")
    public ResponseEntity<List<BookDTO>> findAllByCategoryID(@RequestParam("page") Optional<Integer> pageParam,
                                                                          @RequestParam("search") Optional<String> searchParam,
                                                                          @PathVariable("id") String id) {
        Pageable pageable = pageParam
                .map(integer -> PageRequest.of(integer - 1, PAGE_SIZE))
                .orElseGet(() -> PageRequest.of(0, PAGE_SIZE));

        List<BookDTO> bookDTOList = searchParam
                .map(
                        s -> bookService.findAllByNameContainingIgnoreCaseAndCategory_Id(s, pageable, id).toList()
                )
                .orElseGet(
                        () -> bookService.findAllByCategory_Id(pageable, id).toList()
                );

        return ResponseEntity.ok(
                bookDTOList
        );
    }

    @GetMapping("/books/{bookID}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookDTO> findByID(@PathVariable("bookID") String bookID) {
        return bookService.findById(bookID)
                .map(
                        ResponseEntity::ok

                )
                .orElse(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(
                                        null
                                )
                );
    }

    @GetMapping("/books/image/{filename:.+}")
    public ResponseEntity<Resource> getImagePath(@PathVariable("filename") String filename) {
        Resource file = bookService.load(filename, imageLocation);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @GetMapping("/books/content/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> getContent(@PathVariable("id") String id) {

        return bookService.findById(id).map(
                book -> {
                    Resource file = bookService.load(book.getContentFileName(), contentLocation);

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + book.getContentFileName())
                            .header("Content-Security-Policy", "frame-ancestors 'self' http://localhost:8081")
                            .body(file);
                }
        ).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @GetMapping("/books/download/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@PathVariable("id") String id) {

        return bookService.findById(id).map(
                book -> {
                    Resource file = bookService.load(book.getContentFileName(), contentLocation);

                    return ResponseEntity.ok()
                            .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + book.getContentFileName())
                            .header("Content-Security-Policy", "frame-ancestors 'self' http://localhost:8081")
                            .body(file);
                }
        ).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }




    @GetMapping("/books/{id}/display")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreamingResponseBody> readContent(@PathVariable("id") String bookID) {
        return bookService.findById(bookID)
                .map(
                        book -> ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + book.getContentFileName())
                                .body(bookService.read(book.getContentFileName()))
                )
                .orElse(
                        ResponseEntity.notFound().build()
                );
    }

    @GetMapping("/books/{bookID}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<StreamingResponseBody> downloadFile(@PathVariable("bookID") String bookID) {
        return bookService.findById(bookID)
                .map(book ->
                        ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + book.getContentFileName())
                                .body(bookService.read(book.getContentFileName()))
                )
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping("/books")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<BookDTO> create(@ModelAttribute BookDTO bookDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        bookService.create(bookDTO)
                );
    }

    @PutMapping("/books/{bookID}")
    @PreAuthorize("hasRole('admin')")
    public ResponseEntity<BookDTO> update(@PathVariable("bookID") String bookID, @RequestBody BookDTO bookDTO) {
        return bookService.findById(bookID)
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
