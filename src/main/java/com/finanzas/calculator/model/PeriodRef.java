package com.finanzas.calculator.model;

public record PeriodRef(
        Long id,
        int year,
        int month,
        String label
) {
}
