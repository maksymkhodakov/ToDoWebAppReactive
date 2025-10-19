package com.example.todowebapp.repository;

import com.example.todowebapp.domain.entity.Todo;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.Collection;


@Repository
public interface TodoRepository extends R2dbcRepository<Todo, Long> {

    // simple list by FK
    Flux<Todo> findAllByUserId(Long userId);

    // Ownership-scoped fetch
    Flux<Todo> findAllByIdInAndUserId(Collection<Long> ids, Long userId);

}
