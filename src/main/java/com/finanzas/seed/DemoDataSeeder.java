package com.finanzas.seed;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import com.finanzas.cards.model.CardStatus;
import com.finanzas.cards.model.CreditCard;
import com.finanzas.cards.repository.CreditCardRepository;
import com.finanzas.common.Currency;
import com.finanzas.expenses.model.ExpenseItem;
import com.finanzas.expenses.model.ExpenseType;
import com.finanzas.expenses.model.PaymentMethod;
import com.finanzas.expenses.repository.ExpenseItemRepository;
import com.finanzas.periods.model.ApartmentGoal;
import com.finanzas.periods.model.FinancialPeriod;
import com.finanzas.periods.model.Income;
import com.finanzas.periods.repository.FinancialPeriodRepository;
import com.finanzas.plan.model.AllocationRole;
import com.finanzas.plan.model.PlanAllocation;
import com.finanzas.plan.model.PlanStage;
import com.finanzas.plan.repository.PlanAllocationRepository;
import com.finanzas.users.model.AppUser;
import com.finanzas.users.model.UserRole;
import com.finanzas.users.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(3)
public class DemoDataSeeder implements ApplicationRunner {

    private final AppUserRepository userRepository;
    private final FinancialPeriodRepository periodRepository;
    private final ExpenseItemRepository expenseRepository;
    private final CreditCardRepository cardRepository;
    private final PlanAllocationRepository allocationRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoUsername;

    public DemoDataSeeder(
            AppUserRepository userRepository,
            FinancialPeriodRepository periodRepository,
            ExpenseItemRepository expenseRepository,
            CreditCardRepository cardRepository,
            PlanAllocationRepository allocationRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.demo.username:demo}") String demoUsername) {
        this.userRepository = userRepository;
        this.periodRepository = periodRepository;
        this.expenseRepository = expenseRepository;
        this.cardRepository = cardRepository;
        this.allocationRepository = allocationRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoUsername = demoUsername;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppUser demo = userRepository.findByUsername(demoUsername)
                .orElseGet(() -> userRepository.save(new AppUser(
                        null,
                        demoUsername,
                        passwordEncoder.encode(UUID.randomUUID().toString()),
                        UserRole.DEMO,
                        true,
                        null,
                        null)));

        if (!periodRepository.findAllByOwnerUserIdOrderByPeriodYearDescPeriodMonthDesc(demo.id()).isEmpty()) {
            return;
        }

        YearMonth current = YearMonth.now();
        FinancialPeriod period = periodRepository.save(new FinancialPeriod(
                null,
                demo.id(),
                current.getYear(),
                current.getMonthValue(),
                new Income(
                        amount("1850000"),
                        amount("2400"),
                        amount("1450"),
                        amount("1510"),
                        amount("1425"),
                        amount("3000"),
                        amount("900")),
                new ApartmentGoal(amount("95000"), amount("0.25"), amount("18500")),
                "Datos ficticios para explorar el funcionamiento del plan financiero.",
                null,
                null));

        expenseRepository.saveAll(List.of(
                expense(period.id(), "Vivienda", "Alquiler", "720000", Currency.ARS,
                        PaymentMethod.DEBIT, ExpenseType.ESENCIAL, "Vivienda", 0),
                expense(period.id(), "Servicios", "Expensas y servicios", "185000", Currency.ARS,
                        PaymentMethod.DEBIT, ExpenseType.ESENCIAL, "Servicios", 1),
                expense(period.id(), "Alimentos", "Supermercado", "260000", Currency.ARS,
                        PaymentMethod.CREDIT, ExpenseType.ESENCIAL, "Alimentos", 2),
                expense(period.id(), "Transporte", "Transporte y combustible", "95000", Currency.ARS,
                        PaymentMethod.CREDIT, ExpenseType.VARIABLE, "Transporte", 3),
                expense(period.id(), "Salud", "Cobertura médica", "140000", Currency.ARS,
                        PaymentMethod.DEBIT, ExpenseType.ESENCIAL, "Salud", 4),
                expense(period.id(), "Software", "Herramientas profesionales", "45", Currency.USD,
                        PaymentMethod.CREDIT, ExpenseType.DISCRECIONAL, "Suscripciones", 5),
                expense(period.id(), "Entretenimiento", "Streaming", "32000", Currency.ARS,
                        PaymentMethod.CREDIT, ExpenseType.DISCRECIONAL, "Suscripciones", 6)));

        cardRepository.saveAll(List.of(
                new CreditCard(null, period.id(), "Visa", amount("385000"), Currency.ARS,
                        amount("85000"), amount("89.5"), LocalDate.now().plusDays(12),
                        amount("140000"), CardStatus.PENDIENTE, 0, null, null),
                new CreditCard(null, period.id(), "Mastercard", amount("50"), Currency.USD,
                        amount("50"), BigDecimal.ZERO, LocalDate.now().plusDays(16),
                        amount("50"), CardStatus.PENDIENTE, 1, null, null)));

        allocationRepository.saveAll(List.of(
                allocation(period.id(), PlanStage.SALIDA_DE_TARJETAS, "Gastos esenciales", "0.55",
                        "Cubrir vivienda y obligaciones", AllocationRole.PRESUPUESTO_GASTOS, 0),
                allocation(period.id(), PlanStage.SALIDA_DE_TARJETAS, "Pago de tarjetas", "0.25",
                        "Cancelar los consumos del mes", AllocationRole.NONE, 1),
                allocation(period.id(), PlanStage.SALIDA_DE_TARJETAS, "Fondo de emergencia", "0.10",
                        "Construir una reserva líquida", AllocationRole.NONE, 2),
                allocation(period.id(), PlanStage.SALIDA_DE_TARJETAS, "Gastos personales", "0.10",
                        "Presupuesto flexible", AllocationRole.NONE, 3),
                allocation(period.id(), PlanStage.AHORRO_APARTAMENTO, "Gastos esenciales", "0.50",
                        "Mantener el costo de vida", AllocationRole.NONE, 0),
                allocation(period.id(), PlanStage.AHORRO_APARTAMENTO, "Fondo apartamento", "0.35",
                        "Aumentar el anticipo", AllocationRole.AHORRO_APARTAMENTO, 1),
                allocation(period.id(), PlanStage.AHORRO_APARTAMENTO, "Inversión", "0.10",
                        "Diversificar el ahorro", AllocationRole.NONE, 2),
                allocation(period.id(), PlanStage.AHORRO_APARTAMENTO, "Gastos personales", "0.05",
                        "Mantener un margen mensual", AllocationRole.NONE, 3)));
    }

    private ExpenseItem expense(
            Long periodId,
            String category,
            String detail,
            String amount,
            Currency currency,
            PaymentMethod paymentMethod,
            ExpenseType type,
            String group,
            int sortOrder) {
        return new ExpenseItem(null, periodId, category, detail, amount(amount), currency,
                paymentMethod, true, type, group, null, sortOrder, null, null);
    }

    private PlanAllocation allocation(
            Long periodId,
            PlanStage stage,
            String concept,
            String percentage,
            String objective,
            AllocationRole role,
            int sortOrder) {
        return new PlanAllocation(null, periodId, stage, concept, amount(percentage),
                objective, role, sortOrder, null, null);
    }

    private BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}
