package com.example.todowebapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.todowebapp.domain.entity.Todo;

import java.time.LocalDateTime;
import java.util.List;

public interface TodoRepository extends JpaRepository<Todo, Long> {
    List<Todo> findAllByUserId(Long id);
    Page<Todo> findAllByUserIdAndCreateDateAfter(Long id, LocalDateTime createdDate, Pageable pageable);
}
