package com.finanzas.expenses.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.finanzas.common.Currency;

/**
 * Una fila de la hoja "Gastos mensuales".
 *
 * @param expenseGroup agrupación usada para el gráfico de distribución
 *                     (Apartamento, Servicios, Club y gimnasio, ...)
 */
public record ExpenseItem(
        Long id,
        Long periodId,
        String category,
        String detail,
        BigDecimal amount,
        Currency currency,
        ExpenseType expenseType,
        String expenseGroup,
        String note,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
