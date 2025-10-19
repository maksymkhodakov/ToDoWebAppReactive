package com.example.todowebapp.domain.entity;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;

@Getter
@Setter
public abstract class TimestampEntity {

    @CreatedDate
    @Column("create_date")
    private Instant createDate;

    @LastModifiedDate
    @Column("update_date")
    private Instant updateDate;
}
