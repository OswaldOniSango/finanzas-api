package com.finanzas.actuals.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateMonthlyActualsRequest(
        @NotNull @PositiveOrZero BigDecimal usdExchanged,
        @NotNull @PositiveOrZero BigDecimal arsReceived,
        @NotNull @PositiveOrZero BigDecimal cardPaymentsArs,
        @NotNull @PositiveOrZero BigDecimal cardPaymentsUsd,
        @Size(max = 1000) String notes
) {
}
