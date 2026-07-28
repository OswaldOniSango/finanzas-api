package com.finanzas.expenses.controller;

import com.finanzas.calculator.model.PeriodSummary;
import com.finanzas.expenses.dto.SaveExpenseItemRequest;
import com.finanzas.periods.service.FinancialPeriodService;
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
@RequestMapping("/api/periods/{periodId}/expenses")
public class ExpenseItemController {

    private final FinancialPeriodService service;

    public ExpenseItemController(FinancialPeriodService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PeriodSummary> create(@PathVariable Long periodId,
                                                @Valid @RequestBody SaveExpenseItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addExpense(periodId, request));
    }

    @PutMapping("/{expenseId}")
    public PeriodSummary update(@PathVariable Long periodId,
                                @PathVariable Long expenseId,
                                @Valid @RequestBody SaveExpenseItemRequest request) {
        return service.updateExpense(periodId, expenseId, request);
    }

    @DeleteMapping("/{expenseId}")
    public PeriodSummary delete(@PathVariable Long periodId, @PathVariable Long expenseId) {
        return service.deleteExpense(periodId, expenseId);
    }
}
