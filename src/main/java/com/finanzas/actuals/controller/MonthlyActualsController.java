package com.finanzas.actuals.controller;

import com.finanzas.actuals.dto.MonthlyActualsResponse;
import com.finanzas.actuals.dto.UpdateMonthlyActualsRequest;
import com.finanzas.periods.service.FinancialPeriodService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/periods/{periodId}/actuals")
public class MonthlyActualsController {
    private final FinancialPeriodService service;

    public MonthlyActualsController(FinancialPeriodService service) {
        this.service = service;
    }

    @GetMapping
    public MonthlyActualsResponse get(@PathVariable Long periodId) {
        return service.monthlyActuals(periodId);
    }

    @PutMapping
    public MonthlyActualsResponse update(
            @PathVariable Long periodId,
            @Valid @RequestBody UpdateMonthlyActualsRequest request) {
        return service.updateMonthlyActuals(periodId, request);
    }
}
