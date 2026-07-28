package com.finanzas.users.dto;

import com.finanzas.users.model.AppUser;
import com.finanzas.users.model.UserRole;

public record UserResponse(Long id, String username, UserRole role, boolean enabled) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(user.id(), user.username(), user.role(), user.enabled());
    }
}
