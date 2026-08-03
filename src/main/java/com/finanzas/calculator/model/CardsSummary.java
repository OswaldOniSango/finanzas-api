package com.finanzas.calculator.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Fila TOTAL de la hoja "Tarjetas". Todo se totaliza en USD porque las tarjetas
 * pueden estar en monedas distintas.
 */
public record CardsSummary(
        List<CardLine> lines,
        BigDecimal totalBalanceUsd,
        BigDecimal totalBalanceArs,
        BigDecimal totalMinimumPaymentUsd,
        BigDecimal totalMonthlyPaymentUsd,
        BigDecimal totalMonthlyPaymentArs,
        BigDecimal totalBalanceAfterPaymentUsd,
        Integer estimatedPayoffMonths,
        BigDecimal monthlyLimitUsd,
        BigDecimal monthlyLimitArs,
        BigDecimal creditExpensesUsd,
        BigDecimal creditExpensesArs,
        BigDecimal externalCreditExpensesUsd,
        BigDecimal externalCreditExpensesArs,
        BigDecimal availableLimitUsd,
        BigDecimal availableLimitArs,
        BigDecimal referenceRate
) {
}
