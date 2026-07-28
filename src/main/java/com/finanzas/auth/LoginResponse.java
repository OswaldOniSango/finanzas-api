package com.finanzas.auth;

public record LoginResponse(
        String accessToken,
        long expiresIn,
        String username,
        String role
) {
}
