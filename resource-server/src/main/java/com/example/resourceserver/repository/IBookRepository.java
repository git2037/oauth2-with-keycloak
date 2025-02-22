package com.example.resourceserver.repository;

import com.example.resourceserver.entity.BookEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

public interface IBookRepository extends JpaRepository<BookEntity, String> {
    Stream<BookEntity> findAllByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query(value = "SELECT b.* " +
            "FROM ebook.books b " +
            "RIGHT JOIN ebook.history h ON b.id = h.book_id " +
            "WHERE h.user_id = ?1", nativeQuery = true)
    Stream<BookEntity> getBooksHistoryFromUserID(String userId, Pageable pageable);

    Stream<BookEntity> findAllByCategory_Id(Pageable pageable, String categoryId);
    Stream<BookEntity> findAllByNameContainingIgnoreCaseAndCategory_Id(String name, Pageable pageable, String categoryId);


//    @Query(value = "select b from BookEntity b, HistoryEntity h " +
//            "where h.userId = :userID and ")
//    Stream<BookEntity> findAllByUserID(@Param("userID") String userID);

}
