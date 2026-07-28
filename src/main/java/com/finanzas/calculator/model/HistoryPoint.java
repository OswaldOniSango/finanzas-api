package com.finanzas.calculator.model;

import java.math.BigDecimal;

/**
 * Un mes condensado a los indicadores que tiene sentido comparar en el tiempo.
 */
public record HistoryPoint(
        Long periodId,
        int year,
        int month,
        String label,
        BigDecimal referenceRate,
        BigDecimal totalIncomeUsd,
        BigDecimal conservativeBaseUsd,
        BigDecimal totalExpensesUsd,
        BigDecimal availableAfterExpensesUsd,
        BigDecimal committedIncomeRatio,
        BigDecimal monthlySavingUsd,
        BigDecimal currentSavingsUsd,
        BigDecimal goalProgress,
        BigDecimal cardsBalanceUsd
) {
}
