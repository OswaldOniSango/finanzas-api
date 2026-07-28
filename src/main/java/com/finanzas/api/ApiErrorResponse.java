package com.finanzas.api;

import java.time.LocalDateTime;
import java.util.List;

public record ApiErrorResponse(
        int status,
        String error,
        String message,
        List<FieldErrorResponse> fieldErrors,
        LocalDateTime timestamp
) {

    public static ApiErrorResponse of(int status, String error, String message) {
        return new ApiErrorResponse(status, error, message, null, LocalDateTime.now());
    }
}
