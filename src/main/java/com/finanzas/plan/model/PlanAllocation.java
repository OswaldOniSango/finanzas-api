package com.finanzas.plan.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Una fila de la hoja "Plan mensual": qué porcentaje del ingreso base
 * se asigna a un concepto dentro de una etapa.
 *
 * @param percentage fracción del ingreso base (0..1)
 */
@Table("plan_allocations")
public record PlanAllocation(
        @Id Long id,
        Long periodId,
        PlanStage stage,
        String concept,
        BigDecimal percentage,
        String objective,
        AllocationRole allocationRole,
        int sortOrder,
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt
) {
}
