package com.finanzas.periods.controller;

import java.util.List;

import com.finanzas.calculator.model.HistoryPoint;
import com.finanzas.calculator.model.PeriodRef;
import com.finanzas.calculator.model.PeriodSummary;
import com.finanzas.periods.dto.CreatePeriodRequest;
import com.finanzas.periods.dto.UpdateApartmentGoalRequest;
import com.finanzas.periods.dto.UpdateIncomeRequest;
import com.finanzas.periods.dto.UpdateNotesRequest;
import com.finanzas.periods.service.FinancialPeriodService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/periods")
public class FinancialPeriodController {

    private final FinancialPeriodService service;

    public FinancialPeriodController(FinancialPeriodService service) {
        this.service = service;
    }

    @GetMapping
    public List<PeriodRef> list() {
        return service.listPeriods();
    }

    @GetMapping("/latest")
    public PeriodSummary latest() {
        return service.latestSummary();
    }

    @GetMapping("/history")
    public List<HistoryPoint> history() {
        return service.history();
    }

    @GetMapping("/{periodId}")
    public PeriodSummary detail(@PathVariable Long periodId) {
        return service.summary(periodId);
    }

    @PostMapping
    public ResponseEntity<PeriodSummary> create(@Valid @RequestBody CreatePeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPeriod(request));
    }

    @PutMapping("/{periodId}/income")
    public PeriodSummary updateIncome(@PathVariable Long periodId,
                                      @Valid @RequestBody UpdateIncomeRequest request) {
        return service.updateIncome(periodId, request);
    }

    @PutMapping("/{periodId}/apartment-goal")
    public PeriodSummary updateApartmentGoal(@PathVariable Long periodId,
                                             @Valid @RequestBody UpdateApartmentGoalRequest request) {
        return service.updateApartmentGoal(periodId, request);
    }

    @PutMapping("/{periodId}/notes")
    public PeriodSummary updateNotes(@PathVariable Long periodId,
                                     @Valid @RequestBody UpdateNotesRequest request) {
        return service.updateNotes(periodId, request.notes());
    }

    @DeleteMapping("/{periodId}")
    public ResponseEntity<Void> delete(@PathVariable Long periodId) {
        service.deletePeriod(periodId);
        return ResponseEntity.noContent().build();
    }
}
