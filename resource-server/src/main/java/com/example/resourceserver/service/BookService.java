package com.example.resourceserver.service;

import com.example.resourceserver.dto.BookDTO;
import com.example.resourceserver.dto.CategoryDTO;
import com.example.resourceserver.entity.BookEntity;
import com.example.resourceserver.mapper.BookMapper;
import com.example.resourceserver.repository.IBookRepository;
import com.example.resourceserver.repository.ICategoryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class BookService  {

    @Value("${location.image}")
    private String imageLocation;

    @Value("${location.content}")
    private String contentLocation;

    private final IBookRepository bookRepository;
    private final BookMapper bookMapper;
    private final ICategoryRepository categoryRepository;

    public BookService(IBookRepository bookRepository, BookMapper bookMapper, ICategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
        this.categoryRepository = categoryRepository;
    }

    public Stream<BookDTO> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable).map(
                bookMapper::toDTO
        ).stream();
    }

    public Stream<BookDTO> findAllByNameContainingIgnoreCase(String name, Pageable pageable) {
        return bookRepository.findAllByNameContainingIgnoreCase(name, pageable)
                .map(
                        bookMapper::toDTO
                );
    }

    public BookDTO create(BookDTO bookDTO) {
        bookDTO.setImagePath(saveFile(bookDTO.getImageFile(), imageLocation));
        bookDTO.setContentFilePath(saveFile(bookDTO.getContentFile(), contentLocation));
        bookDTO.setImageFile(null);
        bookDTO.setContentFile(null);

        BookEntity entity = bookMapper.toEntity(bookDTO);

        categoryRepository.findById(bookDTO.getCategoryID())
                .ifPresent(category -> {
                    entity.setCategory(category);
                    bookRepository.save(entity);
                });

        return bookDTO;
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex > 0) {
            return fileName.substring(dotIndex); // Trả về phần mở rộng, ví dụ: ".png"
        }
        return ""; // Nếu không có phần mở rộng
    }

    private String saveFile(MultipartFile file, String location) {
        
        try {
            String newFileName = UUID.randomUUID() + getFileExtension(Objects.requireNonNull(file.getOriginalFilename()));
            Path path = Paths.get(location + File.separatorChar + newFileName);

            // Tạo thư mục nếu chưa có
            Files.createDirectories(path.getParent());

            file.transferTo(path);
            return path.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BookDTO update(BookDTO bookDTO) {
        return bookMapper.toDTO(bookRepository.save(bookMapper.toEntity(bookDTO)));
    }

    public Optional<BookDTO> findById(String id) {
        return bookRepository.findById(id)
                .map(
                        bookMapper::toDTO
                );
    }

    public Resource load(String filePath, String location) {
        try {
            String path = location;
            if (!filePath.startsWith("file://")) {
                path = "file:///" + location + "/" + filePath;
            }

            path = path.replaceAll("\\\\", "/");

            Resource resource = new UrlResource(path);

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    public long count() {
        return bookRepository.count();
    }

    @Transactional(readOnly = true)
    public List<BookDTO> getBooksHistoryFromUserID(String userId, Pageable pageable) {
        return bookRepository.getBooksHistoryFromUserID(userId, pageable)
                .map(bookMapper::toDTO)
                .toList();
    }

    public StreamingResponseBody read(String fileName) {
        return outputStream -> {
            try (FileInputStream fis = new FileInputStream(contentLocation + File.separatorChar + fileName)){
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }

                outputStream.flush();
            }
        };
    }

    public Stream<BookDTO> findAllByNameContainingIgnoreCaseAndCategory_Id(String name, Pageable pageable, String categoryId) {
        return bookRepository.findAllByNameContainingIgnoreCaseAndCategory_Id(name, pageable, categoryId)
                .map(
                        bookMapper::toDTO
                );
    }


    public Stream<BookDTO> findAllByCategory_Id(Pageable pageable, String categoryId){
        return bookRepository.findAllByCategory_Id(pageable, categoryId)
                .map(
                        bookMapper::toDTO
                );
    }
}
