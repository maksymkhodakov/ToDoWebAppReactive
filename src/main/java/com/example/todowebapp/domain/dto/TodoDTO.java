package com.example.todowebapp.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodoDTO {
    private Long id;
    @NotNull
    private String description;
    @NotNull
    private LocalDate dueDate;
    @NotNull
    private boolean checkMark;
    private LocalDate completionDate;
}
