package com.finanzas.cards.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.finanzas.common.Currency;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Una fila de la hoja "Tarjetas".
 *
 * @param annualRatePercent tasa o CFT anual, usado para priorizar qué tarjeta pagar primero
 */
@Table("credit_cards")
public record CreditCard(
        @Id Long id,
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
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt
) {
}
