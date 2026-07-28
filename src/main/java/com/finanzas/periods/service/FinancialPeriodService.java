package com.finanzas.periods.service;

import java.util.Comparator;
import java.util.List;

import com.finanzas.api.ConflictException;
import com.finanzas.api.ResourceNotFoundException;
import com.finanzas.calculator.model.HistoryPoint;
import com.finanzas.calculator.model.PeriodRef;
import com.finanzas.calculator.model.PeriodSummary;
import com.finanzas.calculator.service.PlanCalculator;
import com.finanzas.cards.dto.SaveCreditCardRequest;
import com.finanzas.cards.model.CreditCard;
import com.finanzas.cards.repository.CreditCardRepository;
import com.finanzas.common.PeriodLabels;
import com.finanzas.expenses.dto.SaveExpenseItemRequest;
import com.finanzas.expenses.model.ExpenseItem;
import com.finanzas.expenses.repository.ExpenseItemRepository;
import com.finanzas.periods.dto.CreatePeriodRequest;
import com.finanzas.periods.dto.UpdateApartmentGoalRequest;
import com.finanzas.periods.dto.UpdateIncomeRequest;
import com.finanzas.periods.model.ApartmentGoal;
import com.finanzas.periods.model.FinancialPeriod;
import com.finanzas.periods.model.Income;
import com.finanzas.periods.repository.FinancialPeriodRepository;
import com.finanzas.plan.dto.SavePlanAllocationRequest;
import com.finanzas.plan.model.PlanAllocation;
import com.finanzas.plan.repository.PlanAllocationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Punto único de entrada al plan. Toda mutación devuelve el periodo entero ya
 * recalculado, para que el cliente nunca tenga que replicar una fórmula.
 */
@Service
public class FinancialPeriodService {

    private final FinancialPeriodRepository periodRepository;
    private final ExpenseItemRepository expenseRepository;
    private final CreditCardRepository cardRepository;
    private final PlanAllocationRepository allocationRepository;
    private final PlanCalculator calculator;

    public FinancialPeriodService(FinancialPeriodRepository periodRepository,
                                  ExpenseItemRepository expenseRepository,
                                  CreditCardRepository cardRepository,
                                  PlanAllocationRepository allocationRepository,
                                  PlanCalculator calculator) {
        this.periodRepository = periodRepository;
        this.expenseRepository = expenseRepository;
        this.cardRepository = cardRepository;
        this.allocationRepository = allocationRepository;
        this.calculator = calculator;
    }

    // --- Lectura --------------------------------------------------------

    public List<PeriodRef> listPeriods() {
        return periodRepository.findAll().stream()
                .map(period -> new PeriodRef(
                        period.id(),
                        period.periodYear(),
                        period.periodMonth(),
                        PeriodLabels.full(period.yearMonth())))
                .toList();
    }

    public PeriodSummary summary(Long periodId) {
        FinancialPeriod period = requirePeriod(periodId);
        return calculator.summarize(
                period,
                expenseRepository.findByPeriodId(periodId),
                allocationRepository.findByPeriodId(periodId),
                cardRepository.findByPeriodId(periodId));
    }

    public PeriodSummary latestSummary() {
        FinancialPeriod period = periodRepository.findLatest()
                .orElseThrow(() -> new ResourceNotFoundException("Todavía no hay ningún periodo cargado"));
        return summary(period.id());
    }

    /** Serie histórica en orden cronológico, lista para graficar. */
    public List<HistoryPoint> history() {
        return periodRepository.findAll().stream()
                .sorted(Comparator.comparingInt(FinancialPeriod::periodYear)
                        .thenComparingInt(FinancialPeriod::periodMonth))
                .map(period -> calculator.toHistoryPoint(summary(period.id())))
                .toList();
    }

    // --- Periodos -------------------------------------------------------

    @Transactional
    public PeriodSummary createPeriod(CreatePeriodRequest request) {
        periodRepository.findByYearMonth(request.year(), request.month()).ifPresent(existing -> {
            throw new ConflictException("Ya existe un periodo para " + PeriodLabels.full(existing.yearMonth()));
        });

        FinancialPeriod source = request.cloneFromPeriodId() == null
                ? periodRepository.findLatest().orElse(null)
                : requirePeriod(request.cloneFromPeriodId());

        FinancialPeriod created = periodRepository.insert(new FinancialPeriod(
                null,
                request.year(),
                request.month(),
                source == null ? emptyIncome() : source.income(),
                source == null ? emptyGoal() : source.apartmentGoal(),
                source == null ? null : source.notes(),
                null,
                null));

        if (source != null) {
            copyContents(source.id(), created.id());
        }

        return summary(created.id());
    }

    @Transactional
    public PeriodSummary updateIncome(Long periodId, UpdateIncomeRequest request) {
        requirePeriod(periodId);
        periodRepository.updateIncome(periodId, new Income(
                request.salaryArs(),
                request.salaryUsd(),
                request.referenceRate(),
                request.conservativeBaseUsd()));
        return summary(periodId);
    }

    @Transactional
    public PeriodSummary updateApartmentGoal(Long periodId, UpdateApartmentGoalRequest request) {
        requirePeriod(periodId);
        periodRepository.updateApartmentGoal(periodId, new ApartmentGoal(
                request.targetPriceUsd(),
                request.downPaymentPercent(),
                request.currentSavingsUsd()));
        return summary(periodId);
    }

    @Transactional
    public PeriodSummary updateNotes(Long periodId, String notes) {
        requirePeriod(periodId);
        periodRepository.updateNotes(periodId, notes);
        return summary(periodId);
    }

    @Transactional
    public void deletePeriod(Long periodId) {
        requirePeriod(periodId);
        periodRepository.deleteById(periodId);
    }

    // --- Gastos ---------------------------------------------------------

    @Transactional
    public PeriodSummary addExpense(Long periodId, SaveExpenseItemRequest request) {
        requirePeriod(periodId);
        int sortOrder = request.sortOrder() == null
                ? expenseRepository.nextSortOrder(periodId)
                : request.sortOrder();

        expenseRepository.insert(new ExpenseItem(
                null, periodId, request.category(), request.detail(), request.amount(), request.currency(),
                request.expenseType(), request.expenseGroup(), request.note(), sortOrder, null, null));

        return summary(periodId);
    }

    @Transactional
    public PeriodSummary updateExpense(Long periodId, Long expenseId, SaveExpenseItemRequest request) {
        ExpenseItem existing = requireExpense(periodId, expenseId);

        expenseRepository.update(new ExpenseItem(
                existing.id(), periodId, request.category(), request.detail(), request.amount(), request.currency(),
                request.expenseType(), request.expenseGroup(), request.note(),
                request.sortOrder() == null ? existing.sortOrder() : request.sortOrder(), null, null));

        return summary(periodId);
    }

    @Transactional
    public PeriodSummary deleteExpense(Long periodId, Long expenseId) {
        requireExpense(periodId, expenseId);
        expenseRepository.deleteById(expenseId);
        return summary(periodId);
    }

    // --- Tarjetas -------------------------------------------------------

    @Transactional
    public PeriodSummary addCard(Long periodId, SaveCreditCardRequest request) {
        requirePeriod(periodId);
        int sortOrder = request.sortOrder() == null
                ? cardRepository.nextSortOrder(periodId)
                : request.sortOrder();

        cardRepository.insert(new CreditCard(
                null, periodId, request.name(), request.balance(), request.currency(), request.minimumPayment(),
                request.annualRatePercent(), request.dueDate(), request.monthlyPayment(), request.status(),
                sortOrder, null, null));

        return summary(periodId);
    }

    @Transactional
    public PeriodSummary updateCard(Long periodId, Long cardId, SaveCreditCardRequest request) {
        CreditCard existing = requireCard(periodId, cardId);

        cardRepository.update(new CreditCard(
                existing.id(), periodId, request.name(), request.balance(), request.currency(),
                request.minimumPayment(), request.annualRatePercent(), request.dueDate(), request.monthlyPayment(),
                request.status(), request.sortOrder() == null ? existing.sortOrder() : request.sortOrder(),
                null, null));

        return summary(periodId);
    }

    @Transactional
    public PeriodSummary deleteCard(Long periodId, Long cardId) {
        requireCard(periodId, cardId);
        cardRepository.deleteById(cardId);
        return summary(periodId);
    }

    // --- Plan mensual ---------------------------------------------------

    @Transactional
    public PeriodSummary addAllocation(Long periodId, SavePlanAllocationRequest request) {
        requirePeriod(periodId);
        int sortOrder = request.sortOrder() == null
                ? allocationRepository.nextSortOrder(periodId, request.stage())
                : request.sortOrder();

        allocationRepository.insert(new PlanAllocation(
                null, periodId, request.stage(), request.concept(), request.percentage(), request.objective(),
                request.allocationRole(), sortOrder, null, null));

        return summary(periodId);
    }

    @Transactional
    public PeriodSummary updateAllocation(Long periodId, Long allocationId, SavePlanAllocationRequest request) {
        PlanAllocation existing = requireAllocation(periodId, allocationId);

        allocationRepository.update(new PlanAllocation(
                existing.id(), periodId, request.stage(), request.concept(), request.percentage(),
                request.objective(), request.allocationRole(),
                request.sortOrder() == null ? existing.sortOrder() : request.sortOrder(), null, null));

        return summary(periodId);
    }

    @Transactional
    public PeriodSummary deleteAllocation(Long periodId, Long allocationId) {
        requireAllocation(periodId, allocationId);
        allocationRepository.deleteById(allocationId);
        return summary(periodId);
    }

    // --- Apoyo ----------------------------------------------------------

    private void copyContents(Long sourcePeriodId, Long targetPeriodId) {
        for (ExpenseItem item : expenseRepository.findByPeriodId(sourcePeriodId)) {
            expenseRepository.insert(new ExpenseItem(
                    null, targetPeriodId, item.category(), item.detail(), item.amount(), item.currency(),
                    item.expenseType(), item.expenseGroup(), item.note(), item.sortOrder(), null, null));
        }

        for (CreditCard card : cardRepository.findByPeriodId(sourcePeriodId)) {
            cardRepository.insert(new CreditCard(
                    null, targetPeriodId, card.name(), card.balance(), card.currency(), card.minimumPayment(),
                    card.annualRatePercent(), card.dueDate(), card.monthlyPayment(), card.status(),
                    card.sortOrder(), null, null));
        }

        for (PlanAllocation allocation : allocationRepository.findByPeriodId(sourcePeriodId)) {
            allocationRepository.insert(new PlanAllocation(
                    null, targetPeriodId, allocation.stage(), allocation.concept(), allocation.percentage(),
                    allocation.objective(), allocation.allocationRole(), allocation.sortOrder(), null, null));
        }
    }

    private Income emptyIncome() {
        return new Income(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO,
                java.math.BigDecimal.ONE, java.math.BigDecimal.ZERO);
    }

    private ApartmentGoal emptyGoal() {
        return new ApartmentGoal(java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO, java.math.BigDecimal.ZERO);
    }

    private FinancialPeriod requirePeriod(Long periodId) {
        return periodRepository.findById(periodId)
                .orElseThrow(() -> new ResourceNotFoundException("No existe el periodo " + periodId));
    }

    private ExpenseItem requireExpense(Long periodId, Long expenseId) {
        return expenseRepository.findById(expenseId)
                .filter(item -> item.periodId().equals(periodId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe el gasto " + expenseId + " en el periodo " + periodId));
    }

    private CreditCard requireCard(Long periodId, Long cardId) {
        return cardRepository.findById(cardId)
                .filter(card -> card.periodId().equals(periodId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la tarjeta " + cardId + " en el periodo " + periodId));
    }

    private PlanAllocation requireAllocation(Long periodId, Long allocationId) {
        return allocationRepository.findById(allocationId)
                .filter(allocation -> allocation.periodId().equals(periodId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe la línea de plan " + allocationId + " en el periodo " + periodId));
    }
}
