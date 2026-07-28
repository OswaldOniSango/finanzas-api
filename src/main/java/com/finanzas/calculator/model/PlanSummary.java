package com.finanzas.calculator.model;

import java.math.BigDecimal;
import java.util.List;

/**
 * La pantalla del plan mensual: las etapas más la base que reparten,
 * para que no tenga que pedir los ingresos por separado.
 */
public record PlanSummary(
        BigDecimal conservativeBaseUsd,
        BigDecimal referenceRate,
        List<PlanStageSummary> stages
) {
}
