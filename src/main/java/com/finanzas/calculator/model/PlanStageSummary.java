package com.finanzas.calculator.model;

import java.math.BigDecimal;
import java.util.List;

import com.finanzas.plan.model.PlanStage;

/**
 * Una etapa del plan mensual con sus líneas ya valuadas.
 *
 * @param totalPercentage suma de porcentajes; debería dar 1 (100%)
 * @param balanced        true cuando los porcentajes suman exactamente 100%
 */
public record PlanStageSummary(
        PlanStage stage,
        String label,
        List<PlanLine> lines,
        BigDecimal totalPercentage,
        BigDecimal totalUsd,
        BigDecimal totalArs,
        boolean balanced
) {
}
