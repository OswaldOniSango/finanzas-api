package com.finanzas.calculator.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.finanzas.calculator.model.ApartmentSummary;
import com.finanzas.calculator.model.CardLine;
import com.finanzas.calculator.model.CardsSummary;
import com.finanzas.calculator.model.ExpenseLine;
import com.finanzas.calculator.model.ExpenseSummary;
import com.finanzas.calculator.model.GroupTotal;
import com.finanzas.calculator.model.HistoryPoint;
import com.finanzas.calculator.model.IncomeSummary;
import com.finanzas.calculator.model.PeriodOverview;
import com.finanzas.calculator.model.PeriodRef;
import com.finanzas.calculator.model.PlanLine;
import com.finanzas.calculator.model.PlanStageSummary;
import com.finanzas.calculator.model.PlanSummary;
import com.finanzas.calculator.model.ProjectionPoint;
import com.finanzas.cards.model.CreditCard;
import com.finanzas.common.Currency;
import com.finanzas.common.Money;
import com.finanzas.common.PeriodLabels;
import com.finanzas.expenses.model.ExpenseItem;
import com.finanzas.expenses.model.PaymentMethod;
import com.finanzas.periods.model.FinancialPeriod;
import com.finanzas.periods.model.Income;
import com.finanzas.plan.model.AllocationRole;
import com.finanzas.plan.model.PlanAllocation;
import com.finanzas.plan.model.PlanStage;
import org.springframework.stereotype.Service;

/**
 * Reemplaza a las fórmulas del Excel original: recibe únicamente los datos que
 * el usuario carga y deriva todo lo demás. Ningún valor calculado se persiste,
 * así que cambiar el dólar de referencia recalcula el mes entero.
 *
 * <p>Cada método público corresponde a una pantalla. Las fórmulas están
 * encadenadas entre sí — los gastos necesitan el ingreso, el apartamento
 * necesita los gastos — así que internamente se calcula lo que haga falta;
 * lo que cambia es cuánto de eso se devuelve.
 */
@Service
public class PlanCalculator {

    /** Horizonte de la curva de ahorro: mes 0 hasta mes 36, igual que el Excel. */
    private static final int PROJECTION_MONTHS = 36;

    // --- Una porción por pantalla ---------------------------------------

    public IncomeSummary income(FinancialPeriod period) {
        return summarizeIncome(period.income());
    }

    public PlanSummary plan(FinancialPeriod period, List<PlanAllocation> allocations) {
        IncomeSummary income = income(period);
        return new PlanSummary(
                income.conservativeBaseUsd(),
                income.referenceRate(),
                summarizePlan(allocations, income));
    }

    public ExpenseSummary expenses(FinancialPeriod period,
                                   List<ExpenseItem> items,
                                   List<PlanAllocation> allocations) {
        return summarizeExpenses(items, income(period), allocations);
    }

    public CardsSummary cards(FinancialPeriod period, List<CreditCard> cards, List<ExpenseItem> items) {
        IncomeSummary income = income(period);
        return summarizeCards(cards, items, income.referenceRate(), period.income().cardMonthlyLimitUsd());
    }

    public ApartmentSummary apartment(FinancialPeriod period,
                                      List<ExpenseItem> items,
                                      List<PlanAllocation> allocations) {
        IncomeSummary income = income(period);
        return summarizeApartment(period, allocations, income, summarizeExpenses(items, income, allocations));
    }

    public PeriodOverview overview(FinancialPeriod period,
                                   List<ExpenseItem> items,
                                   List<PlanAllocation> allocations,
                                   List<CreditCard> cards) {

        IncomeSummary income = income(period);
        ExpenseSummary expenses = summarizeExpenses(items, income, allocations);
        CardsSummary cardsSummary = summarizeCards(
                cards, items, income.referenceRate(), period.income().cardMonthlyLimitUsd());
        ApartmentSummary apartment = summarizeApartment(period, allocations, income, expenses);

        return new PeriodOverview(
                periodRef(period),
                period.notes(),
                income.referenceRate(),
                income.totalIncomeUsd(),
                income.conservativeBaseUsd(),
                expenses.totalUsd(),
                expenses.lines().size(),
                expenses.availableAfterExpensesUsd(),
                expenses.committedIncomeRatio(),
                expenses.targetBudgetUsd(),
                expenses.withinBudget(),
                cardsSummary.totalBalanceUsd(),
                cardsSummary.estimatedPayoffMonths(),
                apartment);
    }

    public HistoryPoint historyPoint(FinancialPeriod period,
                                     List<ExpenseItem> items,
                                     List<PlanAllocation> allocations,
                                     List<CreditCard> cards) {

        IncomeSummary income = income(period);
        ExpenseSummary expenses = summarizeExpenses(items, income, allocations);
        ApartmentSummary apartment = summarizeApartment(period, allocations, income, expenses);

        return new HistoryPoint(
                period.id(),
                period.periodYear(),
                period.periodMonth(),
                PeriodLabels.shortLabel(period.yearMonth()),
                income.referenceRate(),
                income.totalIncomeUsd(),
                income.conservativeBaseUsd(),
                expenses.totalUsd(),
                expenses.availableAfterExpensesUsd(),
                expenses.committedIncomeRatio(),
                apartment.monthlySavingUsd(),
                apartment.currentSavingsUsd(),
                apartment.goalProgress(),
                summarizeCards(cards, items, income.referenceRate(), period.income().cardMonthlyLimitUsd())
                        .totalBalanceUsd());
    }

    public PeriodRef periodRef(FinancialPeriod period) {
        return new PeriodRef(
                period.id(),
                period.periodYear(),
                period.periodMonth(),
                PeriodLabels.full(period.yearMonth()));
    }

    // --- Ingresos -------------------------------------------------------

    private IncomeSummary summarizeIncome(Income income) {
        BigDecimal rate = Money.nullSafe(income.referenceRate());
        BigDecimal salaryArs = Money.nullSafe(income.salaryArs());
        BigDecimal salaryUsd = Money.nullSafe(income.salaryUsd());
        BigDecimal base = Money.nullSafe(income.conservativeBaseUsd());

        BigDecimal salaryUsdInArs = salaryUsd.multiply(rate);
        BigDecimal totalArs = salaryArs.add(salaryUsdInArs);

        return new IncomeSummary(
                Money.amount(salaryArs),
                Money.amount(salaryUsd),
                Money.amount(rate),
                Money.amount(Money.nullSafe(income.cardDollarRate())),
                Money.amount(Money.nullSafe(income.payoneerDollarRate())),
                Money.amount(salaryUsdInArs),
                Money.amount(totalArs),
                Money.divide(totalArs, rate, Money.AMOUNT_SCALE),
                Money.amount(base),
                Money.amount(base.multiply(rate)),
                Money.amount(Money.nullSafe(income.cardMonthlyLimitUsd())),
                Money.amount(Money.nullSafe(income.cardMonthlyLimitUsd()).multiply(rate)));
    }

    // --- Plan mensual ---------------------------------------------------

    private List<PlanStageSummary> summarizePlan(List<PlanAllocation> allocations, IncomeSummary income) {
        List<PlanStageSummary> stages = new ArrayList<>();

        for (PlanStage stage : PlanStage.values()) {
            List<PlanAllocation> stageAllocations = allocations.stream()
                    .filter(allocation -> allocation.stage() == stage)
                    .sorted(Comparator.comparingInt(PlanAllocation::sortOrder))
                    .toList();

            List<PlanLine> lines = new ArrayList<>();
            BigDecimal totalPercentage = BigDecimal.ZERO;
            BigDecimal totalUsd = BigDecimal.ZERO;

            for (PlanAllocation allocation : stageAllocations) {
                BigDecimal percentage = Money.nullSafe(allocation.percentage());
                BigDecimal amountUsd = income.conservativeBaseUsd().multiply(percentage);

                lines.add(new PlanLine(
                        allocation.id(),
                        allocation.concept(),
                        Money.ratio(percentage),
                        Money.amount(amountUsd),
                        Money.amount(amountUsd.multiply(income.referenceRate())),
                        allocation.objective(),
                        allocation.allocationRole(),
                        allocation.sortOrder()));

                totalPercentage = totalPercentage.add(percentage);
                totalUsd = totalUsd.add(amountUsd);
            }

            stages.add(new PlanStageSummary(
                    stage,
                    stageLabel(stage),
                    lines,
                    Money.ratio(totalPercentage),
                    Money.amount(totalUsd),
                    Money.amount(totalUsd.multiply(income.referenceRate())),
                    Money.ratio(totalPercentage).compareTo(BigDecimal.ONE.setScale(Money.RATIO_SCALE)) == 0));
        }

        return stages;
    }

    private String stageLabel(PlanStage stage) {
        return switch (stage) {
            case SALIDA_DE_TARJETAS -> "Etapa 1: salir de tarjetas";
            case AHORRO_APARTAMENTO -> "Etapa 2: ahorrar para el apartamento";
        };
    }

    // --- Gastos ---------------------------------------------------------

    private ExpenseSummary summarizeExpenses(List<ExpenseItem> expenses,
                                             IncomeSummary income,
                                             List<PlanAllocation> allocations) {

        BigDecimal rate = income.referenceRate();
        BigDecimal totalArs = BigDecimal.ZERO;
        BigDecimal totalUsd = BigDecimal.ZERO;

        record Converted(ExpenseItem item, BigDecimal ars, BigDecimal usd) {
        }

        List<Converted> converted = new ArrayList<>();
        for (ExpenseItem item : expenses) {
            BigDecimal amount = Money.nullSafe(item.amount());
            BigDecimal ars = item.currency() == Currency.USD ? amount.multiply(rate) : amount;
            BigDecimal usd = item.currency() == Currency.ARS
                    ? Money.divide(amount, rate, Money.AMOUNT_SCALE)
                    : amount;

            converted.add(new Converted(item, ars, usd));
            totalArs = totalArs.add(ars);
            totalUsd = totalUsd.add(usd);
        }

        BigDecimal finalTotalUsd = totalUsd;
        List<ExpenseLine> lines = converted.stream()
                .map(entry -> new ExpenseLine(
                        entry.item().id(),
                        entry.item().category(),
                        entry.item().detail(),
                        Money.amount(entry.item().amount()),
                        entry.item().currency(),
                        entry.item().paymentMethod(),
                        entry.item().countsTowardCardLimit(),
                        entry.item().expenseType(),
                        entry.item().expenseGroup(),
                        entry.item().note(),
                        entry.item().sortOrder(),
                        Money.amount(entry.ars()),
                        Money.amount(entry.usd()),
                        Money.divide(entry.usd(), finalTotalUsd, Money.RATIO_SCALE)))
                .toList();

        List<GroupTotal> byGroup = groupTotals(lines, ExpenseLine::expenseGroup, totalUsd, rate);
        List<GroupTotal> byType = groupTotals(lines, line -> line.expenseType().name(), totalUsd, rate);

        BigDecimal availableUsd = income.conservativeBaseUsd().subtract(totalUsd);
        BigDecimal availableArs = income.conservativeBaseArs().subtract(totalArs);
        BigDecimal creditExpensesUsd = converted.stream()
                .filter(entry -> entry.item().paymentMethod() == PaymentMethod.CREDIT)
                .map(Converted::usd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal creditExpensesArs = converted.stream()
                .filter(entry -> entry.item().paymentMethod() == PaymentMethod.CREDIT)
                .map(Converted::ars)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ownCardExpensesUsd = converted.stream()
                .filter(entry -> entry.item().paymentMethod() == PaymentMethod.CREDIT)
                .filter(entry -> entry.item().countsTowardCardLimit())
                .map(Converted::usd)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal ownCardExpensesArs = converted.stream()
                .filter(entry -> entry.item().paymentMethod() == PaymentMethod.CREDIT)
                .filter(entry -> entry.item().countsTowardCardLimit())
                .map(Converted::ars)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal externalCreditExpensesUsd = creditExpensesUsd.subtract(ownCardExpensesUsd);
        BigDecimal externalCreditExpensesArs = creditExpensesArs.subtract(ownCardExpensesArs);
        BigDecimal availableCardLimitUsd = income.cardMonthlyLimitUsd().subtract(ownCardExpensesUsd);
        BigDecimal availableCardLimitArs = income.cardMonthlyLimitArs().subtract(ownCardExpensesArs);

        BigDecimal budgetUsd = allocations.stream()
                .filter(allocation -> allocation.allocationRole() == AllocationRole.PRESUPUESTO_GASTOS)
                .findFirst()
                .map(allocation -> income.conservativeBaseUsd().multiply(Money.nullSafe(allocation.percentage())))
                .orElse(null);

        // Sin una línea marcada como presupuesto, "dentro del presupuesto" degrada
        // a la pregunta más básica: ¿el gasto entra dentro del ingreso base?
        boolean withinBudget = budgetUsd == null
                ? availableUsd.signum() >= 0
                : totalUsd.compareTo(budgetUsd) <= 0;

        return new ExpenseSummary(
                lines,
                byGroup,
                byType,
                Money.amount(totalArs),
                Money.amount(totalUsd),
                income.conservativeBaseArs(),
                income.conservativeBaseUsd(),
                Money.amount(availableArs),
                Money.amount(availableUsd),
                Money.divide(totalUsd, income.conservativeBaseUsd(), Money.RATIO_SCALE),
                budgetUsd == null ? null : Money.amount(budgetUsd.multiply(rate)),
                budgetUsd == null ? null : Money.amount(budgetUsd),
                budgetUsd == null ? null : Money.amount(budgetUsd.multiply(rate).subtract(totalArs)),
                budgetUsd == null ? null : Money.amount(budgetUsd.subtract(totalUsd)),
                withinBudget,
                income.cardMonthlyLimitArs(),
                income.cardMonthlyLimitUsd(),
                Money.amount(creditExpensesArs),
                Money.amount(creditExpensesUsd),
                Money.amount(ownCardExpensesArs),
                Money.amount(ownCardExpensesUsd),
                Money.amount(externalCreditExpensesArs),
                Money.amount(externalCreditExpensesUsd),
                Money.amount(availableCardLimitArs),
                Money.amount(availableCardLimitUsd));
    }

    private List<GroupTotal> groupTotals(List<ExpenseLine> lines,
                                         java.util.function.Function<ExpenseLine, String> keyExtractor,
                                         BigDecimal totalUsd,
                                         BigDecimal rate) {

        Map<String, BigDecimal> totals = new LinkedHashMap<>();
        for (ExpenseLine line : lines) {
            String key = keyExtractor.apply(line);
            totals.merge(key, line.amountUsd(), BigDecimal::add);
        }

        return totals.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> new GroupTotal(
                        entry.getKey(),
                        Money.amount(entry.getValue().multiply(rate)),
                        Money.amount(entry.getValue()),
                        Money.divide(entry.getValue(), totalUsd, Money.RATIO_SCALE)))
                .toList();
    }

    // --- Tarjetas -------------------------------------------------------

    private CardsSummary summarizeCards(List<CreditCard> cards,
                                        List<ExpenseItem> items,
                                        BigDecimal rate,
                                        BigDecimal monthlyLimitUsdValue) {
        List<CardLine> lines = new ArrayList<>();
        BigDecimal totalBalanceUsd = BigDecimal.ZERO;
        BigDecimal totalMinimumUsd = BigDecimal.ZERO;
        BigDecimal totalPaymentUsd = BigDecimal.ZERO;
        BigDecimal totalAfterUsd = BigDecimal.ZERO;
        Integer slowestPayoff = null;

        for (CreditCard card : cards) {
            BigDecimal balance = Money.nullSafe(card.balance());
            BigDecimal payment = Money.nullSafe(card.monthlyPayment());
            BigDecimal minimum = Money.nullSafe(card.minimumPayment());
            BigDecimal afterPayment = Money.atLeastZero(balance.subtract(payment));

            BigDecimal balanceUsd = toUsd(balance, card.currency(), rate);
            BigDecimal paymentUsd = toUsd(payment, card.currency(), rate);
            BigDecimal minimumUsd = toUsd(minimum, card.currency(), rate);
            BigDecimal afterUsd = toUsd(afterPayment, card.currency(), rate);

            // Estimación sin intereses: sirve para ordenar prioridades, no como cronograma exacto.
            Integer payoffMonths = null;
            if (balance.signum() > 0 && payment.signum() > 0) {
                payoffMonths = balance.divide(payment, 0, RoundingMode.CEILING).intValue();
                if (slowestPayoff == null || payoffMonths > slowestPayoff) {
                    slowestPayoff = payoffMonths;
                }
            }

            lines.add(new CardLine(
                    card.id(),
                    card.name(),
                    Money.amount(balance),
                    card.currency(),
                    Money.amount(minimum),
                    Money.amount(card.annualRatePercent()),
                    card.dueDate(),
                    Money.amount(payment),
                    card.status(),
                    card.sortOrder(),
                    Money.amount(toArs(balance, card.currency(), rate)),
                    Money.amount(balanceUsd),
                    Money.amount(toArs(payment, card.currency(), rate)),
                    Money.amount(paymentUsd),
                    Money.amount(afterPayment),
                    Money.amount(afterUsd),
                    payoffMonths));

            totalBalanceUsd = totalBalanceUsd.add(balanceUsd);
            totalMinimumUsd = totalMinimumUsd.add(minimumUsd);
            totalPaymentUsd = totalPaymentUsd.add(paymentUsd);
            totalAfterUsd = totalAfterUsd.add(afterUsd);
        }

        BigDecimal monthlyLimitUsd = Money.nullSafe(monthlyLimitUsdValue);
        BigDecimal creditExpensesUsd = items.stream()
                .filter(item -> item.paymentMethod() == PaymentMethod.CREDIT)
                .filter(ExpenseItem::countsTowardCardLimit)
                .map(item -> toUsd(Money.nullSafe(item.amount()), item.currency(), rate))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal externalCreditExpensesUsd = items.stream()
                .filter(item -> item.paymentMethod() == PaymentMethod.CREDIT)
                .filter(item -> !item.countsTowardCardLimit())
                .map(item -> toUsd(Money.nullSafe(item.amount()), item.currency(), rate))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal availableLimitUsd = monthlyLimitUsd.subtract(creditExpensesUsd);

        return new CardsSummary(
                lines,
                Money.amount(totalBalanceUsd),
                Money.amount(totalBalanceUsd.multiply(rate)),
                Money.amount(totalMinimumUsd),
                Money.amount(totalPaymentUsd),
                Money.amount(totalPaymentUsd.multiply(rate)),
                Money.amount(totalAfterUsd),
                slowestPayoff,
                Money.amount(monthlyLimitUsd),
                Money.amount(monthlyLimitUsd.multiply(rate)),
                Money.amount(creditExpensesUsd),
                Money.amount(creditExpensesUsd.multiply(rate)),
                Money.amount(externalCreditExpensesUsd),
                Money.amount(externalCreditExpensesUsd.multiply(rate)),
                Money.amount(availableLimitUsd),
                Money.amount(availableLimitUsd.multiply(rate)));
    }

    private BigDecimal toUsd(BigDecimal amount, Currency currency, BigDecimal rate) {
        return currency == Currency.ARS ? Money.divide(amount, rate, Money.AMOUNT_SCALE) : amount;
    }

    private BigDecimal toArs(BigDecimal amount, Currency currency, BigDecimal rate) {
        return currency == Currency.USD ? amount.multiply(rate) : amount;
    }

    // --- Apartamento ----------------------------------------------------

    private ApartmentSummary summarizeApartment(FinancialPeriod period,
                                                List<PlanAllocation> allocations,
                                                IncomeSummary income,
                                                ExpenseSummary expenses) {

        BigDecimal targetPrice = Money.nullSafe(period.apartmentGoal().targetPriceUsd());
        BigDecimal downPercent = Money.nullSafe(period.apartmentGoal().downPaymentPercent());
        BigDecimal savings = Money.nullSafe(period.apartmentGoal().currentSavingsUsd());

        BigDecimal cashGoal = targetPrice.multiply(downPercent);
        BigDecimal pending = Money.atLeastZero(cashGoal.subtract(savings));

        BigDecimal planned = allocations.stream()
                .filter(allocation -> allocation.allocationRole() == AllocationRole.AHORRO_APARTAMENTO)
                .findFirst()
                .map(allocation -> income.conservativeBaseUsd().multiply(Money.nullSafe(allocation.percentage())))
                .orElse(BigDecimal.ZERO);

        // MAX(MIN(meta del plan, disponible real), 0): nunca se ahorra plata que no existe.
        BigDecimal monthlySaving = Money.atLeastZero(
                Money.min(planned, expenses.availableAfterExpensesUsd()));

        BigDecimal estimatedMonths = null;
        Integer estimatedMonthsRounded = null;
        String estimatedCompletion = null;

        if (monthlySaving.signum() > 0 && pending.signum() > 0) {
            estimatedMonths = pending.divide(monthlySaving, 1, RoundingMode.HALF_UP);
            estimatedMonthsRounded = pending.divide(monthlySaving, 0, RoundingMode.CEILING).intValue();
            estimatedCompletion = PeriodLabels.full(period.yearMonth().plusMonths(estimatedMonthsRounded));
        } else if (pending.signum() == 0) {
            estimatedMonths = BigDecimal.ZERO.setScale(1);
            estimatedMonthsRounded = 0;
            estimatedCompletion = PeriodLabels.full(period.yearMonth());
        }

        YearMonth start = period.yearMonth();
        List<ProjectionPoint> projection = new ArrayList<>();
        for (int month = 0; month <= PROJECTION_MONTHS; month++) {
            BigDecimal accumulated = savings.add(monthlySaving.multiply(BigDecimal.valueOf(month)));
            projection.add(new ProjectionPoint(
                    month,
                    PeriodLabels.shortLabel(start.plusMonths(month)),
                    Money.amount(accumulated)));
        }

        return new ApartmentSummary(
                Money.amount(targetPrice),
                Money.ratio(downPercent),
                Money.amount(cashGoal),
                Money.amount(savings),
                Money.amount(planned),
                expenses.availableAfterExpensesUsd(),
                Money.amount(monthlySaving),
                Money.amount(pending),
                Money.divide(savings, cashGoal, Money.RATIO_SCALE),
                estimatedMonths,
                estimatedMonthsRounded,
                estimatedCompletion,
                projection);
    }
}
