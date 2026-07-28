package com.finanzas.api;

public record FieldErrorResponse(
        String field,
        String message
) {
}
