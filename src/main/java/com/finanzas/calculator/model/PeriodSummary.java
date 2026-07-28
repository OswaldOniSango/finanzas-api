package com.finanzas.calculator.model;

import java.util.List;

/**
 * Todo el plan de un mes, con cada valor derivado ya calculado.
 * Es el equivalente a abrir el Excel con las fórmulas resueltas.
 */
public record PeriodSummary(
        PeriodRef period,
        String notes,
        IncomeSummary income,
        ExpenseSummary expenses,
        List<PlanStageSummary> plan,
        CardsSummary cards,
        ApartmentSummary apartment
) {
}
