package com.finanzas.periods.service;

import java.math.BigDecimal;
import java.util.List;

import com.finanzas.actuals.dto.MonthlyActualsResponse;
import com.finanzas.actuals.dto.UpdateMonthlyActualsRequest;
import com.finanzas.actuals.model.MonthlyActuals;
import com.finanzas.actuals.repository.MonthlyActualsRepository;
import com.finanzas.api.ConflictException;
import com.finanzas.api.ResourceNotFoundException;
import com.finanzas.calculator.model.ApartmentSummary;
import com.finanzas.calculator.model.CardsSummary;
import com.finanzas.calculator.model.ExpenseSummary;
import com.finanzas.calculator.model.HistoryPoint;
import com.finanzas.calculator.model.IncomeSummary;
import com.finanzas.calculator.model.PeriodOverview;
import com.finanzas.calculator.model.PeriodRef;
import com.finanzas.calculator.model.PlanSummary;
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
import com.finanzas.users.service.CurrentUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Punto único de entrada al plan.
 *
 * <p>Cada pantalla pide su propia porción y cada mutación devuelve sólo la
 * porción que tocó, ya recalculada. Como las fórmulas encadenan las pantallas
 * entre sí (cambiar el dólar mueve gastos, plan y apartamento), el cliente
 * vuelve a pedir la porción de la pantalla al entrar en ella.
 */
@Service
public class FinancialPeriodService {

    private final FinancialPeriodRepository periodRepository;
    private final ExpenseItemRepository expenseRepository;
    private final CreditCardRepository cardRepository;
    private final PlanAllocationRepository allocationRepository;
    private final PlanCalculator calculator;
    private final CurrentUserService currentUserService;
    private final MonthlyActualsRepository actualsRepository;

    public FinancialPeriodService(FinancialPeriodRepository periodRepository,
                                  ExpenseItemRepository expenseRepository,
                                  CreditCardRepository cardRepository,
                                  PlanAllocationRepository allocationRepository,
                                  PlanCalculator calculator,
                                  CurrentUserService currentUserService,
                                  MonthlyActualsRepository actualsRepository) {
        this.periodRepository = periodRepository;
        this.expenseRepository = expenseRepository;
        this.cardRepository = cardRepository;
        this.allocationRepository = allocationRepository;
        this.calculator = calculator;
        this.currentUserService = currentUserService;
        this.actualsRepository = actualsRepository;
    }

    // --- Periodos -------------------------------------------------------

    public List<PeriodRef> listPeriods() {
        return periodRepository.findAllByOwnerUserIdOrderByPeriodYearDescPeriodMonthDesc(currentUserId()).stream()
                .map(calculator::periodRef)
                .toList();
    }

    public PeriodRef latestPeriod() {
        return calculator.periodRef(periodRepository.findFirstByOwnerUserIdOrderByPeriodYearDescPeriodMonthDesc(currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Todavía no hay ningún periodo cargado")));
    }

    /** Serie histórica en orden cronológico, lista para graficar. */
    public List<HistoryPoint> history() {
        return periodRepository.findAllByOwnerUserIdOrderByPeriodYearAscPeriodMonthAsc(currentUserId()).stream()
                .map(period -> calculator.historyPoint(
                        period, expensesOf(period), allocationsOf(period), cardsOf(period)))
                .toList();
    }

    @Transactional
    public PeriodRef createPeriod(CreatePeriodRequest request) {
        Long ownerUserId = currentUserId();
        periodRepository.findByOwnerUserIdAndPeriodYearAndPeriodMonth(
                ownerUserId, request.year(), request.month()).ifPresent(existing -> {
            throw new ConflictException("Ya existe un periodo para " + PeriodLabels.full(existing.yearMonth()));
        });

        FinancialPeriod source = request.cloneFromPeriodId() == null
                ? periodRepository.findFirstByOwnerUserIdOrderByPeriodYearDescPeriodMonthDesc(ownerUserId).orElse(null)
                : requirePeriod(request.cloneFromPeriodId());

        FinancialPeriod created = periodRepository.save(new FinancialPeriod(
                null,
                ownerUserId,
                request.year(),
                request.month(),
                source == null ? emptyIncome() : source.income(),
                source == null ? emptyGoal() : source.apartmentGoal(),
                source == null ? null : source.notes(),
                null,
                null));

        if (source != null) {
            copyContents(source, created.id());
        }

        return calculator.periodRef(created);
    }

    @Transactional
    public void deletePeriod(Long periodId) {
        // Los hijos se van con el ON DELETE CASCADE declarado en las migraciones.
        periodRepository.delete(requirePeriod(periodId));
    }

    // --- Resumen --------------------------------------------------------

    public PeriodOverview overview(Long periodId) {
        FinancialPeriod period = requirePeriod(periodId);
        return calculator.overview(period, expensesOf(period), allocationsOf(period), cardsOf(period));
    }

    @Transactional
    public PeriodOverview updateNotes(Long periodId, String notes) {
        FinancialPeriod period = periodRepository.save(requirePeriod(periodId).withNotes(notes));
        return calculator.overview(period, expensesOf(period), allocationsOf(period), cardsOf(period));
    }

    // --- Ingresos -------------------------------------------------------

    public IncomeSummary income(Long periodId) {
        return calculator.income(requirePeriod(periodId));
    }

    @Transactional
    public IncomeSummary updateIncome(Long periodId, UpdateIncomeRequest request) {
        FinancialPeriod period = requirePeriod(periodId);
        return calculator.income(periodRepository.save(period.withIncome(new Income(
                request.salaryArs(),
                request.salaryUsd(),
                request.referenceRate(),
                request.cardDollarRate(),
                request.payoneerDollarRate(),
                request.conservativeBaseUsd()))));
    }

    // --- Cierre mensual -------------------------------------------------

    public MonthlyActualsResponse monthlyActuals(Long periodId) {
        requirePeriod(periodId);
        return actualsRepository.findByPeriodId(periodId)
                .map(MonthlyActualsResponse::from)
                .orElseGet(MonthlyActualsResponse::empty);
    }

    @Transactional
    public MonthlyActualsResponse updateMonthlyActuals(Long periodId, UpdateMonthlyActualsRequest request) {
        requirePeriod(periodId);
        MonthlyActuals existing = actualsRepository.findByPeriodId(periodId).orElse(null);
        MonthlyActuals saved = actualsRepository.save(new MonthlyActuals(
                existing == null ? null : existing.id(), periodId,
                request.usdExchanged(), request.arsReceived(),
                request.cardPaymentsArs(), request.cardPaymentsUsd(), request.notes(),
                existing == null ? null : existing.createdAt(),
                existing == null ? null : existing.updatedAt()));
        return MonthlyActualsResponse.from(saved);
    }

    // --- Gastos ---------------------------------------------------------

    public ExpenseSummary expenses(Long periodId) {
        FinancialPeriod period = requirePeriod(periodId);
        return calculator.expenses(period, expensesOf(period), allocationsOf(period));
    }

    @Transactional
    public ExpenseSummary addExpense(Long periodId, SaveExpenseItemRequest request) {
        FinancialPeriod period = requirePeriod(periodId);
        List<ExpenseItem> current = expensesOf(period);
        int sortOrder = request.sortOrder() == null
                ? nextSortOrder(current.stream().mapToInt(ExpenseItem::sortOrder))
                : request.sortOrder();

        expenseRepository.save(new ExpenseItem(
                null, periodId, request.category(), request.detail(), request.amount(), request.currency(),
                request.paymentMethod(), request.expenseType(), request.expenseGroup(), request.note(), sortOrder, null, null));

        return calculator.expenses(period, expensesOf(period), allocationsOf(period));
    }

    @Transactional
    public ExpenseSummary updateExpense(Long periodId, Long expenseId, SaveExpenseItemRequest request) {
        FinancialPeriod period = requirePeriod(periodId);
        ExpenseItem existing = requireExpense(periodId, expenseId);

        expenseRepository.save(new ExpenseItem(
                existing.id(), periodId, request.category(), request.detail(), request.amount(), request.currency(),
                request.paymentMethod(), request.expenseType(), request.expenseGroup(), request.note(),
                request.sortOrder() == null ? existing.sortOrder() : request.sortOrder(),
                existing.createdAt(), existing.updatedAt()));

        return calculator.expenses(period, expensesOf(period), allocationsOf(period));
    }

    @Transactional
    public ExpenseSummary deleteExpense(Long periodId, Long expenseId) {
        FinancialPeriod period = requirePeriod(periodId);
        expenseRepository.delete(requireExpense(periodId, expenseId));
        return calculator.expenses(period, expensesOf(period), allocationsOf(period));
    }

    // --- Tarjetas -------------------------------------------------------

    public CardsSummary cards(Long periodId) {
        FinancialPeriod period = requirePeriod(periodId);
        return calculator.cards(period, cardsOf(period));
    }

    @Transactional
    public CardsSummary addCard(Long periodId, SaveCreditCardRequest request) {
        FinancialPeriod period = requirePeriod(periodId);
        int sortOrder = request.sortOrder() == null
                ? nextSortOrder(cardsOf(period).stream().mapToInt(CreditCard::sortOrder))
                : request.sortOrder();

        cardRepository.save(new CreditCard(
                null, periodId, request.name(), request.balance(), request.currency(), request.minimumPayment(),
                request.annualRatePercent(), request.dueDate(), request.monthlyPayment(), request.status(),
                sortOrder, null, null));

        return calculator.cards(period, cardsOf(period));
    }

    @Transactional
    public CardsSummary updateCard(Long periodId, Long cardId, SaveCreditCardRequest request) {
        FinancialPeriod period = requirePeriod(periodId);
        CreditCard existing = requireCard(periodId, cardId);

        cardRepository.save(new CreditCard(
                existing.id(), periodId, request.name(), request.balance(), request.currency(),
                request.minimumPayment(), request.annualRatePercent(), request.dueDate(), request.monthlyPayment(),
                request.status(), request.sortOrder() == null ? existing.sortOrder() : request.sortOrder(),
                existing.createdAt(), existing.updatedAt()));

        return calculator.cards(period, cardsOf(period));
    }

    @Transactional
    public CardsSummary deleteCard(Long periodId, Long cardId) {
        FinancialPeriod period = requirePeriod(periodId);
        cardRepository.delete(requireCard(periodId, cardId));
        return calculator.cards(period, cardsOf(period));
    }

    // --- Plan mensual ---------------------------------------------------

    public PlanSummary plan(Long periodId) {
        FinancialPeriod period = requirePeriod(periodId);
        return calculator.plan(period, allocationsOf(period));
    }

    @Transactional
    public PlanSummary addAllocation(Long periodId, SavePlanAllocationRequest request) {
        FinancialPeriod period = requirePeriod(periodId);
        int sortOrder = request.sortOrder() == null
                ? nextSortOrder(allocationsOf(period).stream()
                        .filter(allocation -> allocation.stage() == request.stage())
                        .mapToInt(PlanAllocation::sortOrder))
                : request.sortOrder();

        allocationRepository.save(new PlanAllocation(
                null, periodId, request.stage(), request.concept(), request.percentage(), request.objective(),
                request.allocationRole(), sortOrder, null, null));

        return calculator.plan(period, allocationsOf(period));
    }

    @Transactional
    public PlanSummary updateAllocation(Long periodId, Long allocationId,
                                                   SavePlanAllocationRequest request) {
        FinancialPeriod period = requirePeriod(periodId);
        PlanAllocation existing = requireAllocation(periodId, allocationId);

        allocationRepository.save(new PlanAllocation(
                existing.id(), periodId, request.stage(), request.concept(), request.percentage(),
                request.objective(), request.allocationRole(),
                request.sortOrder() == null ? existing.sortOrder() : request.sortOrder(),
                existing.createdAt(), existing.updatedAt()));

        return calculator.plan(period, allocationsOf(period));
    }

    @Transactional
    public PlanSummary deleteAllocation(Long periodId, Long allocationId) {
        FinancialPeriod period = requirePeriod(periodId);
        allocationRepository.delete(requireAllocation(periodId, allocationId));
        return calculator.plan(period, allocationsOf(period));
    }

    // --- Apartamento ----------------------------------------------------

    public ApartmentSummary apartment(Long periodId) {
        FinancialPeriod period = requirePeriod(periodId);
        return calculator.apartment(period, expensesOf(period), allocationsOf(period));
    }

    @Transactional
    public ApartmentSummary updateApartmentGoal(Long periodId, UpdateApartmentGoalRequest request) {
        FinancialPeriod period = periodRepository.save(requirePeriod(periodId).withApartmentGoal(new ApartmentGoal(
                request.targetPriceUsd(),
                request.downPaymentPercent(),
                request.currentSavingsUsd())));

        return calculator.apartment(period, expensesOf(period), allocationsOf(period));
    }

    // --- Apoyo ----------------------------------------------------------

    private List<ExpenseItem> expensesOf(FinancialPeriod period) {
        return expenseRepository.findByPeriodIdOrderBySortOrderAscIdAsc(period.id());
    }

    private List<CreditCard> cardsOf(FinancialPeriod period) {
        return cardRepository.findByPeriodIdOrderBySortOrderAscIdAsc(period.id());
    }

    private List<PlanAllocation> allocationsOf(FinancialPeriod period) {
        return allocationRepository.findByPeriodIdOrderByStageAscSortOrderAscIdAsc(period.id());
    }

    private void copyContents(FinancialPeriod source, Long targetPeriodId) {
        expenseRepository.saveAll(expensesOf(source).stream()
                .map(item -> new ExpenseItem(
                        null, targetPeriodId, item.category(), item.detail(), item.amount(), item.currency(),
                        item.paymentMethod(), item.expenseType(), item.expenseGroup(), item.note(), item.sortOrder(), null, null))
                .toList());

        cardRepository.saveAll(cardsOf(source).stream()
                .map(card -> new CreditCard(
                        null, targetPeriodId, card.name(), card.balance(), card.currency(),
                        card.minimumPayment(), card.annualRatePercent(), card.dueDate(),
                        card.monthlyPayment(), card.status(), card.sortOrder(), null, null))
                .toList());

        allocationRepository.saveAll(allocationsOf(source).stream()
                .map(allocation -> new PlanAllocation(
                        null, targetPeriodId, allocation.stage(), allocation.concept(),
                        allocation.percentage(), allocation.objective(), allocation.allocationRole(),
                        allocation.sortOrder(), null, null))
                .toList());
    }

    private int nextSortOrder(java.util.stream.IntStream existing) {
        return existing.max().orElse(-1) + 1;
    }

    private Income emptyIncome() {
        return new Income(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ONE, BigDecimal.ZERO);
    }

    private ApartmentGoal emptyGoal() {
        return new ApartmentGoal(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
    }

    private FinancialPeriod requirePeriod(Long periodId) {
        return periodRepository.findByIdAndOwnerUserId(periodId, currentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("No existe el periodo " + periodId));
    }

    private Long currentUserId() {
        return currentUserService.require().id();
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
