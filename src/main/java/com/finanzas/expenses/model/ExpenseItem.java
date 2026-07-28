package com.finanzas.expenses.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.finanzas.common.Currency;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Una fila de la hoja "Gastos mensuales".
 *
 * @param expenseGroup agrupación usada para el gráfico de distribución
 *                     (Apartamento, Servicios, Club y gimnasio, ...)
 */
@Table("expense_items")
public record ExpenseItem(
        @Id Long id,
        Long periodId,
        String category,
        String detail,
        BigDecimal amount,
        Currency currency,
        PaymentMethod paymentMethod,
        ExpenseType expenseType,
        String expenseGroup,
        String note,
        int sortOrder,
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt
) {
}
