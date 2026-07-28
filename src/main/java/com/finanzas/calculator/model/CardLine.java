package com.finanzas.calculator.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.finanzas.cards.model.CardStatus;
import com.finanzas.common.Currency;

/**
 * Una tarjeta con el saldo posterior al pago ya resuelto (columna H del Excel)
 * y una estimación de en cuántos meses queda cancelada al ritmo actual.
 */
public record CardLine(
        Long id,
        String name,
        BigDecimal balance,
        Currency currency,
        BigDecimal minimumPayment,
        BigDecimal annualRatePercent,
        LocalDate dueDate,
        BigDecimal monthlyPayment,
        CardStatus status,
        int sortOrder,
        BigDecimal balanceArs,
        BigDecimal balanceUsd,
        BigDecimal monthlyPaymentArs,
        BigDecimal monthlyPaymentUsd,
        BigDecimal balanceAfterPayment,
        BigDecimal balanceAfterPaymentUsd,
        Integer payoffMonths
) {
}
