package com.finanzas.periods.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateIncomeRequest(
        @NotNull @PositiveOrZero BigDecimal salaryArs,
        @NotNull @PositiveOrZero BigDecimal salaryUsd,
        @NotNull @PositiveOrZero BigDecimal referenceRate,
        @NotNull @PositiveOrZero BigDecimal cardDollarRate,
        @NotNull @PositiveOrZero BigDecimal payoneerDollarRate,
        @NotNull @PositiveOrZero BigDecimal conservativeBaseUsd
) {
}
