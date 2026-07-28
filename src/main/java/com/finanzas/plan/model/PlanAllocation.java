package com.finanzas.plan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Una fila de la hoja "Plan mensual": qué porcentaje del ingreso base
 * se asigna a un concepto dentro de una etapa.
 *
 * @param percentage fracción del ingreso base (0..1)
 */
public record PlanAllocation(
        Long id,
        Long periodId,
        PlanStage stage,
        String concept,
        BigDecimal percentage,
        String objective,
        AllocationRole allocationRole,
        int sortOrder,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
