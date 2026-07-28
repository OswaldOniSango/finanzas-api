package com.finanzas.cards.controller;

import com.finanzas.calculator.model.PeriodSummary;
import com.finanzas.cards.dto.SaveCreditCardRequest;
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
@RequestMapping("/api/periods/{periodId}/cards")
public class CreditCardController {

    private final FinancialPeriodService service;

    public CreditCardController(FinancialPeriodService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<PeriodSummary> create(@PathVariable Long periodId,
                                                @Valid @RequestBody SaveCreditCardRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.addCard(periodId, request));
    }

    @PutMapping("/{cardId}")
    public PeriodSummary update(@PathVariable Long periodId,
                                @PathVariable Long cardId,
                                @Valid @RequestBody SaveCreditCardRequest request) {
        return service.updateCard(periodId, cardId, request);
    }

    @DeleteMapping("/{cardId}")
    public PeriodSummary delete(@PathVariable Long periodId, @PathVariable Long cardId) {
        return service.deleteCard(periodId, cardId);
    }
}
