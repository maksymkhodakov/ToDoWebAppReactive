package com.example.todowebapp.domain.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
@Table("todos")
public class Todo extends TimestampEntity {

    @Id
    @Column("id")
    private Long id;

    @Column("description")
    private String description;

    @Column("due_date")
    private LocalDate dueDate;

    @Column("check_mark")
    private boolean checkMark;

    @Column("completion_date")
    private LocalDate completionDate;

    @Column("user_id")
    private Long userId;   // FK to users.id
}
