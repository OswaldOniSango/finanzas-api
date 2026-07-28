package com.finanzas.periods.model;

import java.time.LocalDateTime;
import java.time.YearMonth;

/**
 * Un mes del plan financiero. Cada periodo guarda sus propios supuestos
 * (ingresos, dólar de referencia y meta del apartamento) para poder comparar
 * la evolución mes a mes sin que un cambio actual reescriba el pasado.
 */
public record FinancialPeriod(
        Long id,
        int periodYear,
        int periodMonth,
        Income income,
        ApartmentGoal apartmentGoal,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    public YearMonth yearMonth() {
        return YearMonth.of(periodYear, periodMonth);
    }
}
