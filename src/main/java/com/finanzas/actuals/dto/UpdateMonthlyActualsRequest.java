package com.finanzas.actuals.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record UpdateMonthlyActualsRequest(
        @NotNull @PositiveOrZero BigDecimal actualPayoneerRate,
        @NotNull @PositiveOrZero BigDecimal usdExchanged,
        @NotNull @PositiveOrZero BigDecimal cardPaymentsArs,
        @Size(max = 1000) String notes
) {
}
