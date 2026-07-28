package com.finanzas.calculator.model;

import java.math.BigDecimal;

/**
 * Los indicadores que muestra la pantalla de resumen, y nada más.
 *
 * <p>Es un recorte deliberado: toma un número de cada área en vez de anidar los
 * resúmenes completos de ingresos, gastos, plan y tarjetas. El apartamento sí va
 * entero porque esa pantalla dibuja la curva de ahorro.
 */
public record PeriodOverview(
        PeriodRef period,
        String notes,
        BigDecimal referenceRate,
        BigDecimal totalIncomeUsd,
        BigDecimal conservativeBaseUsd,
        BigDecimal totalExpensesUsd,
        int expenseCount,
        BigDecimal availableAfterExpensesUsd,
        BigDecimal committedIncomeRatio,
        BigDecimal targetBudgetUsd,
        boolean withinBudget,
        BigDecimal cardsBalanceUsd,
        Integer cardsPayoffMonths,
        ApartmentSummary apartment
) {
}
