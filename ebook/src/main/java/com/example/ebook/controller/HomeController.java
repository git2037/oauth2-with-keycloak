package com.example.ebook.controller;

import com.example.ebook.dto.BookDTO;
import com.example.ebook.dto.CategoryDTO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Controller
public class HomeController {

    @Value("${base-resource-server-uri}")
    private String baseResourceServerUri;

    private final WebClient webClient;

    public HomeController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/")
    public String index(Model model, @RequestParam("search") Optional<String> optionalSearch) {

        List<BookDTO> books = webClient.get()
                .uri("/api/books")
                .retrieve()
                .bodyToFlux(BookDTO.class)
                .map(book -> {
                    book.setImagePath(baseResourceServerUri + "/api/books/image/" + book.getImageFileName());
                    return book; // Trả về đối tượng đã chỉnh sửa
                })
                .collectList()
                .block();

        List<CategoryDTO> categories = webClient.get()
                .uri("/api/categories")
                .retrieve()
                .bodyToFlux(CategoryDTO.class)
                .collectList()
                .block();

        Long numberOfBooks = webClient.get()
                .uri("/api/books/count")
                .retrieve()
                .bodyToMono(Long.class)
                .block();

        model.addAttribute("books", books);
        model.addAttribute("categories", categories);
        model.addAttribute("numberOfBooks", numberOfBooks);

        return "index";
    }

    @GetMapping("/categories/{id}/books")
    public String findBooksByCategoryId(@PathVariable String id, Model model) {
        List<BookDTO> books = webClient.get()
                .uri("/api/categories/" + id + "/books")
                .retrieve()
                .bodyToFlux(BookDTO.class)
                .collectList()
                .block();

        model.addAttribute("books", books);
        return "index";
    }

    @GetMapping("/books/{id}")
    public String findBookById(@PathVariable String id, Model model, HttpServletResponse response) throws IOException {
        BookDTO book = webClient.get()
                .uri("/api/books/" + id)
                .attributes(clientRegistrationId("Keycloak"))
                .retrieve()
                .bodyToMono(BookDTO.class)
                .map( bookDTO -> {
                    bookDTO.setContentFilePath("http://localhost:8081/books/content/" + id);
                    return bookDTO; // Trả về đối tượng đã chỉnh sửa
                })
                .block();

        List<CategoryDTO> categories = webClient.get()
                .uri("/api/categories")
                .attributes(clientRegistrationId("Keycloak"))
                .retrieve()
                .bodyToFlux(CategoryDTO.class)
                .collectList()
                .block();

        model.addAttribute("book", book);
        assert book != null;
        model.addAttribute("downloadLink", book.getContentFilePath().replace("content", "download"));
        model.addAttribute("categories", categories);
        return "book";
    }

    @GetMapping("/books/content/{id}")
    @ResponseBody
    public ResponseEntity<Resource> proxyReadContent(@PathVariable("id") String bookID, HttpServletResponse response) {

        BookDTO book = webClient.get()
                .uri("/api/books/" + bookID)
                .attributes(clientRegistrationId("Keycloak"))
                .retrieve()
                .bodyToMono(BookDTO.class)
                .block();

        response.setHeader("Content-Security-Policy", "frame-ancestors 'self' http://localhost:8081");

        Resource rs =  webClient.get()
                .uri("/api/books/content/{id}", bookID)
                .attributes(clientRegistrationId("Keycloak"))
                .retrieve()
                .bodyToMono(Resource.class)  // Lấy dữ liệu dưới dạng byte[]
                .block(); // Block để đợi kết quả trả về (hoặc sử dụng Mono/Flux nếu không muốn block)

        assert book != null;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + book.getContentFileName())
                .header("Content-Security-Policy", "frame-ancestors 'self' http://localhost:8081")
                .body(rs);
    }

    @GetMapping("/books/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> download(@PathVariable("id") String bookID, HttpServletResponse response) {

        BookDTO book = webClient.get()
                .uri("/api/books/" + bookID)
                .attributes(clientRegistrationId("Keycloak"))
                .retrieve()
                .bodyToMono(BookDTO.class)
                .block();

        response.setHeader("Content-Security-Policy", "frame-ancestors 'self' http://localhost:8081");

        Resource rs =  webClient.get()
                .uri("/api/books/download/{id}", bookID)
                .attributes(clientRegistrationId("Keycloak"))
                .retrieve()
                .bodyToMono(Resource.class)  // Lấy dữ liệu dưới dạng byte[]
                .block(); // Block để đợi kết quả trả về (hoặc sử dụng Mono/Flux nếu không muốn block)

        assert book != null;
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf")
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + book.getContentFileName())
                .body(rs);
    }
}
