package com.finanzas.periods.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * @param cloneFromPeriodId si viene, el mes nuevo arranca como copia exacta de ese periodo
 *                          (ingresos, gastos, tarjetas y plan). Si es null, se usa el mes más
 *                          reciente como base; si tampoco hay, el periodo nace vacío.
 */
public record CreatePeriodRequest(
        @Min(2000) @Max(2200) int year,
        @Min(1) @Max(12) int month,
        Long cloneFromPeriodId
) {
}
