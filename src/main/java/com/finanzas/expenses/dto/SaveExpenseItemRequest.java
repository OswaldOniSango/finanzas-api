package com.finanzas.expenses.dto;

import java.math.BigDecimal;

import com.finanzas.common.Currency;
import com.finanzas.expenses.model.ExpenseType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SaveExpenseItemRequest(
        @NotBlank @Size(max = 100) String category,
        @Size(max = 255) String detail,
        @NotNull @PositiveOrZero BigDecimal amount,
        @NotNull Currency currency,
        @NotNull ExpenseType expenseType,
        @NotBlank @Size(max = 100) String expenseGroup,
        @Size(max = 500) String note,
        Integer sortOrder
) {
}
