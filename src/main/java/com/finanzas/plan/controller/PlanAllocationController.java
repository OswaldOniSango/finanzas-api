package com.finanzas.plan.controller;

import com.finanzas.calculator.model.PeriodSummary;
import com.finanzas.periods.service.FinancialPeriodService;
import com.finanzas.plan.dto.SavePlanAllocationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/periods/{periodId}/allocations")
public class PlanAllocationController {

    private final FinancialPeriodService service;

    public PlanAllocationController(FinancialPeriodService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PeriodSummary> create(@PathVariable Long periodId,
                                                @Valid @RequestBody SavePlanAllocationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addAllocation(periodId, request));
    }

    @PutMapping("/{allocationId}")
    public PeriodSummary update(@PathVariable Long periodId,
                                @PathVariable Long allocationId,
                                @Valid @RequestBody SavePlanAllocationRequest request) {
        return service.updateAllocation(periodId, allocationId, request);
    }

    @DeleteMapping("/{allocationId}")
    public PeriodSummary delete(@PathVariable Long periodId, @PathVariable Long allocationId) {
        return service.deleteAllocation(periodId, allocationId);
    }
}
