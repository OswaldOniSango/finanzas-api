package com.finanzas.calculator.model;

import java.math.BigDecimal;

import com.finanzas.plan.model.AllocationRole;

public record PlanLine(
        Long id,
        String concept,
        BigDecimal percentage,
        BigDecimal amountUsd,
        BigDecimal amountArs,
        String objective,
        AllocationRole allocationRole,
        int sortOrder
) {
}
