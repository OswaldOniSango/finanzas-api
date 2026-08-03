package com.finanzas.cards.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record UpdateCardLimitRequest(
        @NotNull @PositiveOrZero BigDecimal monthlyLimitUsd
) {
}
