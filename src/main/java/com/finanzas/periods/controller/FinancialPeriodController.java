package com.finanzas.periods.controller;

import java.util.List;

import com.finanzas.calculator.model.ApartmentSummary;
import com.finanzas.calculator.model.HistoryPoint;
import com.finanzas.calculator.model.IncomeSummary;
import com.finanzas.calculator.model.PeriodOverview;
import com.finanzas.calculator.model.PeriodRef;
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

/**
 * Un GET por pantalla. Cada uno devuelve sólo lo que esa pantalla dibuja.
 */
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
    public PeriodRef latest() {
        return service.latestPeriod();
    }

    @GetMapping("/history")
    public List<HistoryPoint> history() {
        return service.history();
    }

    @PostMapping
    public ResponseEntity<PeriodRef> create(@Valid @RequestBody CreatePeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createPeriod(request));
    }

    @DeleteMapping("/{periodId}")
    public ResponseEntity<Void> delete(@PathVariable Long periodId) {
        service.deletePeriod(periodId);
        return ResponseEntity.noContent().build();
    }

    // --- Resumen --------------------------------------------------------

    @GetMapping("/{periodId}/overview")
    public PeriodOverview overview(@PathVariable Long periodId) {
        return service.overview(periodId);
    }

    @PutMapping("/{periodId}/notes")
    public PeriodOverview updateNotes(@PathVariable Long periodId,
                                      @Valid @RequestBody UpdateNotesRequest request) {
        return service.updateNotes(periodId, request.notes());
    }

    // --- Ingresos -------------------------------------------------------

    @GetMapping("/{periodId}/income")
    public IncomeSummary income(@PathVariable Long periodId) {
        return service.income(periodId);
    }

    @PutMapping("/{periodId}/income")
    public IncomeSummary updateIncome(@PathVariable Long periodId,
                                      @Valid @RequestBody UpdateIncomeRequest request) {
        return service.updateIncome(periodId, request);
    }

    // --- Apartamento ----------------------------------------------------

    @GetMapping("/{periodId}/apartment")
    public ApartmentSummary apartment(@PathVariable Long periodId) {
        return service.apartment(periodId);
    }

    @PutMapping("/{periodId}/apartment-goal")
    public ApartmentSummary updateApartmentGoal(@PathVariable Long periodId,
                                                @Valid @RequestBody UpdateApartmentGoalRequest request) {
        return service.updateApartmentGoal(periodId, request);
    }
}
