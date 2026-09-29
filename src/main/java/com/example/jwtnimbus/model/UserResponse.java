package com.example.jwtnimbus.model;

import com.example.jwtnimbus.entity.User;
import java.time.Instant;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String image,
        String role,
        Instant createdAt
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getImage(),
                user.getRole().name(),
                user.getCreatedAt()
        );
    }
}
