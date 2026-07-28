package com.finanzas.cards.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.finanzas.cards.model.CardStatus;
import com.finanzas.common.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SaveCreditCardRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @PositiveOrZero BigDecimal balance,
        @NotNull Currency currency,
        @NotNull @PositiveOrZero BigDecimal minimumPayment,
        @NotNull @PositiveOrZero BigDecimal annualRatePercent,
        LocalDate dueDate,
        @NotNull @PositiveOrZero BigDecimal monthlyPayment,
        @NotNull CardStatus status,
        Integer sortOrder
) {
}
