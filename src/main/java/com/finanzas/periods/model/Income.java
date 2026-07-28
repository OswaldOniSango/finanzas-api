package com.finanzas.periods.model;

import java.math.BigDecimal;

/**
 * Hoja "Ingresos" del plan original.
 *
 * @param salaryArs           sueldo cobrado en pesos
 * @param salaryUsd           sueldo cobrado en dólares
 * @param referenceRate       dólar de referencia (ARS por USD)
 * @param conservativeBaseUsd base conservadora en USD sobre la que se arma el plan
 */
public record Income(
        BigDecimal salaryArs,
        BigDecimal salaryUsd,
        BigDecimal referenceRate,
        BigDecimal conservativeBaseUsd
) {
}
