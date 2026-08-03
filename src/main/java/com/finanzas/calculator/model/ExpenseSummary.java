package com.finanzas.calculator.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * Bloque "Resumen" de la hoja de gastos mensuales.
 *
 * @param committedIncomeRatio  porción del ingreso base ya comprometida en gastos
 * @param targetBudgetUsd       presupuesto objetivo tomado de la línea del plan
 *                              marcada como PRESUPUESTO_GASTOS
 * @param withinBudget          true cuando el gasto real no supera ese presupuesto
 */
public record ExpenseSummary(
        List<ExpenseLine> lines,
        List<GroupTotal> byGroup,
        List<GroupTotal> byType,
        BigDecimal totalArs,
        BigDecimal totalUsd,
        BigDecimal baseIncomeArs,
        BigDecimal baseIncomeUsd,
        BigDecimal availableAfterExpensesArs,
        BigDecimal availableAfterExpensesUsd,
        BigDecimal committedIncomeRatio,
        BigDecimal targetBudgetArs,
        BigDecimal targetBudgetUsd,
        BigDecimal differenceVsBudgetArs,
        BigDecimal differenceVsBudgetUsd,
        boolean withinBudget,
        BigDecimal cardMonthlyLimitArs,
        BigDecimal cardMonthlyLimitUsd,
        BigDecimal creditExpensesArs,
        BigDecimal creditExpensesUsd,
        BigDecimal ownCardExpensesArs,
        BigDecimal ownCardExpensesUsd,
        BigDecimal externalCreditExpensesArs,
        BigDecimal externalCreditExpensesUsd,
        BigDecimal availableCardLimitArs,
        BigDecimal availableCardLimitUsd
) {
}
