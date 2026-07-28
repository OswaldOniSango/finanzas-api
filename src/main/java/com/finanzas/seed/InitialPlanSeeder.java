package com.finanzas.seed;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

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
import com.finanzas.users.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Carga el primer periodo con los datos del plan original
 * (plan_compra_apartamento_con_gastos.xlsm). Sólo corre con la base vacía,
 * así que nunca pisa datos ya cargados.
 */
@Component
@Order(2)
public class InitialPlanSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialPlanSeeder.class);

    private static final String NOTES = """
            1. No hacer compras nuevas en cuotas durante la etapa de deuda.
            2. Pagar primero la tarjeta con mayor CFT.
            3. Transferir el ahorro del apartamento al cobrar.
            4. Mantener el presupuesto personal para evitar recaídas.
            5. Actualizar este plan una vez por semana.""";

    private final FinancialPeriodRepository periodRepository;
    private final ExpenseItemRepository expenseRepository;
    private final CreditCardRepository cardRepository;
    private final PlanAllocationRepository allocationRepository;
    private final boolean seedEnabled;
    private final AppUserRepository userRepository;
    private final String initialUsername;

    public InitialPlanSeeder(FinancialPeriodRepository periodRepository,
                             ExpenseItemRepository expenseRepository,
                             CreditCardRepository cardRepository,
                             PlanAllocationRepository allocationRepository,
                             AppUserRepository userRepository,
                             @Value("${app.security.username}") String initialUsername,
                             @Value("${app.seed.enabled:true}") boolean seedEnabled) {
        this.periodRepository = periodRepository;
        this.expenseRepository = expenseRepository;
        this.cardRepository = cardRepository;
        this.allocationRepository = allocationRepository;
        this.userRepository = userRepository;
        this.initialUsername = initialUsername;
        this.seedEnabled = seedEnabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!seedEnabled || periodRepository.count() > 0) {
            return;
        }

        YearMonth current = YearMonth.now();
        Long ownerUserId = userRepository.findByUsername(initialUsername).orElseThrow().id();

        FinancialPeriod period = periodRepository.save(new FinancialPeriod(
                null,
                ownerUserId,
                current.getYear(),
                current.getMonthValue(),
                new Income(
                        new BigDecimal("664000"),
                        new BigDecimal("3895"),
                        new BigDecimal("1525"),
                        new BigDecimal("1525"),
                        new BigDecimal("1525"),
                        new BigDecimal("4100")),
                new ApartmentGoal(
                        new BigDecimal("100000"),
                        new BigDecimal("0.30"),
                        BigDecimal.ZERO),
                NOTES,
                null,
                null));

        seedAllocations(period.id());
        seedExpenses(period.id());
        seedCards(period.id());

        log.info("Periodo inicial cargado a partir del plan original: {}/{}",
                current.getMonthValue(), current.getYear());
    }

    private void seedAllocations(Long periodId) {
        List<PlanAllocation> allocations = List.of(
                allocation(periodId, PlanStage.SALIDA_DE_TARJETAS, "Gastos esenciales", "0.50",
                        "Vivienda, comida y obligaciones", AllocationRole.PRESUPUESTO_GASTOS, 0),
                allocation(periodId, PlanStage.SALIDA_DE_TARJETAS, "Pago de tarjetas", "0.30",
                        "Eliminar saldos, sin compras nuevas", AllocationRole.NONE, 1),
                allocation(periodId, PlanStage.SALIDA_DE_TARJETAS, "Gastos personales", "0.10",
                        "Límite mensual con débito", AllocationRole.NONE, 2),
                allocation(periodId, PlanStage.SALIDA_DE_TARJETAS, "Fondo de emergencia", "0.10",
                        "Evitar volver a usar crédito", AllocationRole.NONE, 3),

                allocation(periodId, PlanStage.AHORRO_APARTAMENTO, "Gastos esenciales", "0.50",
                        "Mantener el estilo de vida", AllocationRole.NONE, 0),
                allocation(periodId, PlanStage.AHORRO_APARTAMENTO, "Fondo apartamento", "0.35",
                        "Transferencia automática al cobrar", AllocationRole.AHORRO_APARTAMENTO, 1),
                allocation(periodId, PlanStage.AHORRO_APARTAMENTO, "Gastos personales", "0.10",
                        "Controlados con débito", AllocationRole.NONE, 2),
                allocation(periodId, PlanStage.AHORRO_APARTAMENTO, "Emergencias / mantenimiento", "0.05",
                        "Reserva separada", AllocationRole.NONE, 3));

        allocationRepository.saveAll(allocations);
    }

    private void seedExpenses(Long periodId) {
        List<ExpenseItem> expenses = List.of(
                expense(periodId, "Apartamento", "Alquiler / hipoteca", "1395000", Currency.ARS,
                        ExpenseType.ESENCIAL, "Apartamento", 0),
                expense(periodId, "Servicios", "Luz", "40000", Currency.ARS,
                        ExpenseType.ESENCIAL, "Servicios", 1),
                expense(periodId, "Servicios", "Expensas", "419200", Currency.ARS,
                        ExpenseType.ESENCIAL, "Servicios", 2),
                expense(periodId, "Servicios", "Gas", "60000", Currency.ARS,
                        ExpenseType.ESENCIAL, "Servicios", 3),
                expense(periodId, "Servicios", "Internet", "100000", Currency.ARS,
                        ExpenseType.ESENCIAL, "Servicios", 4),
                expense(periodId, "Servicios", "ABL", "70105.53", Currency.ARS,
                        ExpenseType.ESENCIAL, "Servicios", 5),
                expense(periodId, "Club", "2 hijas y yo", "0", Currency.ARS,
                        ExpenseType.FAMILIAR, "Club y gimnasio", 6),
                expense(periodId, "Gimnasio", "Mi esposa y yo", "100000", Currency.ARS,
                        ExpenseType.PERSONAL, "Club y gimnasio", 7),
                expense(periodId, "Software", "Codex", "20", Currency.USD,
                        ExpenseType.DISCRECIONAL, "Software y nube", 8),
                expense(periodId, "Software", "Claude", "20", Currency.USD,
                        ExpenseType.DISCRECIONAL, "Software y nube", 9),
                expense(periodId, "Nube", "iCloud", "10", Currency.USD,
                        ExpenseType.DISCRECIONAL, "Software y nube", 10),
                expense(periodId, "Streaming", "Disney+", "24000", Currency.ARS,
                        ExpenseType.DISCRECIONAL, "Streaming", 11),
                expense(periodId, "Streaming", "HBO Max", "11700", Currency.ARS,
                        ExpenseType.DISCRECIONAL, "Streaming", 12),
                expense(periodId, "Teléfono", "Claro", "21800", Currency.ARS,
                        ExpenseType.VARIABLE, "Otros", 13),
                expense(periodId, "Teléfono", "Movistar", "42000", Currency.ARS,
                        ExpenseType.VARIABLE, "Otros", 14));

        expenseRepository.saveAll(expenses);
    }

    private void seedCards(Long periodId) {
        List<CreditCard> cards = List.of(
                card(periodId, "Tarjeta 1", Currency.ARS, 0),
                card(periodId, "Tarjeta 2", Currency.ARS, 1),
                card(periodId, "Tarjeta 3", Currency.USD, 2));

        cardRepository.saveAll(cards);
    }

    private PlanAllocation allocation(Long periodId, PlanStage stage, String concept, String percentage,
                                      String objective, AllocationRole role, int sortOrder) {
        return new PlanAllocation(null, periodId, stage, concept, new BigDecimal(percentage),
                objective, role, sortOrder, null, null);
    }

    private ExpenseItem expense(Long periodId, String category, String detail, String amount, Currency currency,
                                ExpenseType type, String group, int sortOrder) {
        return new ExpenseItem(null, periodId, category, detail, new BigDecimal(amount), currency,
                PaymentMethod.DEBIT, type, group, null, sortOrder, null, null);
    }

    private CreditCard card(Long periodId, String name, Currency currency, int sortOrder) {
        return new CreditCard(null, periodId, name, BigDecimal.ZERO, currency, BigDecimal.ZERO,
                BigDecimal.ZERO, null, BigDecimal.ZERO, CardStatus.PENDIENTE, sortOrder, null, null);
    }
}
