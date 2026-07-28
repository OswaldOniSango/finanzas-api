package com.finanzas.calculator.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Hoja "Apartamento".
 *
 * @param plannedMonthlySavingUsd  lo que el plan asigna al fondo del apartamento
 * @param monthlySavingUsd         lo que realmente se puede ahorrar: el menor entre
 *                                 el plan y el disponible después de gastos, nunca negativo
 * @param estimatedMonths          meses hasta la meta al ritmo actual; null si no hay ahorro
 */
public record ApartmentSummary(
        BigDecimal targetPriceUsd,
        BigDecimal downPaymentPercent,
        BigDecimal cashGoalUsd,
        BigDecimal currentSavingsUsd,
        BigDecimal plannedMonthlySavingUsd,
        BigDecimal availableAfterExpensesUsd,
        BigDecimal monthlySavingUsd,
        BigDecimal pendingUsd,
        BigDecimal goalProgress,
        BigDecimal estimatedMonths,
        Integer estimatedMonthsRounded,
        String estimatedCompletion,
        List<ProjectionPoint> projection
) {
}
