package com.example.ebookmanager.controller;

import com.example.ebookmanager.dto.BookDTO;
import com.example.ebookmanager.dto.CategoryDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Controller("/admin")
public class HomeController {

    @Value("${base-resource-server-uri}")
    private String baseResourceServerUri;

    private final WebClient webClient;

    public HomeController(WebClient webClient) {
        this.webClient = webClient;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/admin/books";
    }

    @GetMapping("/books")
    public String displayBook(Model model, @RequestParam("action") Optional<String> actionParam) {

        if (actionParam.isPresent() && actionParam.get().equals("add")) {

            List<CategoryDTO> categories = webClient.get()
                    .uri("/api/categories")
                    .retrieve()
                    .bodyToFlux(CategoryDTO.class)
                    .collectList()
                    .block();

            model.addAttribute("book", new BookDTO());
            model.addAttribute("categories", categories);

            return "book/detail";

        } else {
            List<BookDTO> bookDTOS = webClient.get()
                    .uri("/api/books")
                    .retrieve()
                    .bodyToFlux(BookDTO.class)
                    .collectList()
                    .block();

            model.addAttribute("books", bookDTOS);
            return "book/list";
        }
    }


    @PostMapping("/books/add")
    public String addBook(@RequestParam("name") String name,
                          @RequestParam("authors") String authors,
                          @RequestParam("introduction") String description,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          @RequestParam("categoryID") String categoryID,
                          @RequestParam("contentFile") MultipartFile contentFile) {

        // Tạo MultipartBodyBuilder để xây dựng form-data
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();

        // Thêm các tham số vào body của form-data
        bodyBuilder.part("name", name);
        bodyBuilder.part("authors", authors);
        bodyBuilder.part("introduction", description);
        bodyBuilder.part("categoryID", categoryID);

        // Thêm các tệp vào body form-data
        bodyBuilder.part("imageFile", imageFile.getResource())
                .header("Content-Type", imageFile.getContentType());
        bodyBuilder.part("contentFile", contentFile.getResource())
                .header("Content-Type", contentFile.getContentType());

        // Gửi yêu cầu POST đến API với các tệp đính kèm
        webClient.post()
                .uri("/api/books")
                .attributes(clientRegistrationId("Keycloak"))
                .contentType(MediaType.MULTIPART_FORM_DATA)  // Đảm bảo gửi dữ liệu dưới dạng form-data
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .retrieve()
                .bodyToMono(BookDTO.class)
                .block(); // Chặn cho đến khi có phản hồi từ API

        return "redirect:/books?status=success";
    }


    @GetMapping("/books/{id}")
    public String findBookById(@PathVariable String id, Model model) {
        BookDTO book = webClient.get()
                .uri("/api/books/" + id)
                .attributes(clientRegistrationId("Keycloak"))
                .retrieve()
                .bodyToMono(BookDTO.class)
                .map( bookDTO -> {
                    bookDTO.setContentFilePath(baseResourceServerUri + "/api/books/" + bookDTO.getId() + "/display");
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
        model.addAttribute("downloadLink", book.getContentFilePath().replace("display", "download"));
        model.addAttribute("categories", categories);
        return "list";
    }
}
