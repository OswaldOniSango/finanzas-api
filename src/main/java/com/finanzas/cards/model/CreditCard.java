package com.finanzas.cards.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.finanzas.common.Currency;

/**
 * Una fila de la hoja "Tarjetas".
 *
 * @param annualRatePercent tasa o CFT anual, usado para priorizar qué tarjeta pagar primero
 */
public record CreditCard(
        Long id,
        Long periodId,
        String name,
        BigDecimal balance,
        Currency currency,
        BigDecimal minimumPayment,
        BigDecimal annualRatePercent,
        LocalDate dueDate,
        BigDecimal monthlyPayment,
        CardStatus status,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
