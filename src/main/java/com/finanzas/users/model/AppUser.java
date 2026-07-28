package com.finanzas.users.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Table("users")
public record AppUser(
        @Id Long id,
        String username,
        String passwordHash,
        UserRole role,
        boolean enabled,
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt
) {
}
