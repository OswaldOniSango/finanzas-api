package com.finanzas.periods.model;

import java.time.LocalDateTime;
import java.time.YearMonth;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Un mes del plan financiero. Cada periodo guarda sus propios supuestos
 * (ingresos, dólar de referencia y meta del apartamento) para poder comparar
 * la evolución mes a mes sin que un cambio actual reescriba el pasado.
 *
 * <p>Gastos, tarjetas y líneas de plan son agregados aparte que apuntan acá por
 * {@code periodId}: si fueran colecciones embebidas, cada guardado borraría y
 * volvería a insertar sus filas, cambiándoles el id en cada edición.
 */
@Table("financial_periods")
public record FinancialPeriod(
        @Id Long id,
        int periodYear,
        int periodMonth,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY) Income income,
        @Embedded(onEmpty = Embedded.OnEmpty.USE_EMPTY, prefix = "apartment_") ApartmentGoal apartmentGoal,
        String notes,
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt
) {

    public YearMonth yearMonth() {
        return YearMonth.of(periodYear, periodMonth);
    }

    public FinancialPeriod withIncome(Income newIncome) {
        return new FinancialPeriod(id, periodYear, periodMonth, newIncome, apartmentGoal, notes, createdAt, updatedAt);
    }

    public FinancialPeriod withApartmentGoal(ApartmentGoal newGoal) {
        return new FinancialPeriod(id, periodYear, periodMonth, income, newGoal, notes, createdAt, updatedAt);
    }

    public FinancialPeriod withNotes(String newNotes) {
        return new FinancialPeriod(id, periodYear, periodMonth, income, apartmentGoal, newNotes, createdAt, updatedAt);
    }
}
