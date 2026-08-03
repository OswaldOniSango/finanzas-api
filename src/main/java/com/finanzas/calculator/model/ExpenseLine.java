package com.finanzas.calculator.model;

import java.math.BigDecimal;

import com.finanzas.common.Currency;
import com.finanzas.expenses.model.ExpenseType;
import com.finanzas.expenses.model.PaymentMethod;

/**
 * Una fila de gastos con sus dos conversiones ya resueltas,
 * como hacían las columnas E y F del Excel.
 */
public record ExpenseLine(
        Long id,
        String category,
        String detail,
        BigDecimal amount,
        Currency currency,
        PaymentMethod paymentMethod,
        boolean countsTowardCardLimit,
        ExpenseType expenseType,
        String expenseGroup,
        String note,
        int sortOrder,
        BigDecimal amountArs,
        BigDecimal amountUsd,
        BigDecimal shareOfTotal
) {
}
