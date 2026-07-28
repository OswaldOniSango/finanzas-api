package com.finanzas.plan.dto;

import java.math.BigDecimal;

import com.finanzas.plan.model.AllocationRole;
import com.finanzas.plan.model.PlanStage;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SavePlanAllocationRequest(
        @NotNull PlanStage stage,
        @NotBlank @Size(max = 120) String concept,
        @NotNull @PositiveOrZero @DecimalMax("1.0") BigDecimal percentage,
        @Size(max = 500) String objective,
        @NotNull AllocationRole allocationRole,
        Integer sortOrder
) {
}
