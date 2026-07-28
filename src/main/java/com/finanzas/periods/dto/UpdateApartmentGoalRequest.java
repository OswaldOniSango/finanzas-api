package com.finanzas.periods.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateApartmentGoalRequest(
        @NotNull @PositiveOrZero BigDecimal targetPriceUsd,
        @NotNull @PositiveOrZero @DecimalMax("1.0") BigDecimal downPaymentPercent,
        @NotNull @PositiveOrZero BigDecimal currentSavingsUsd
) {
}
