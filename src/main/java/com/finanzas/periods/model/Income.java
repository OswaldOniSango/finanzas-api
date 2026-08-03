package com.finanzas.periods.model;

import java.math.BigDecimal;

/**
 * Hoja "Ingresos" del plan original.
 *
 * @param salaryArs           sueldo cobrado en pesos
 * @param salaryUsd           sueldo cobrado en dólares
 * @param referenceRate       dólar de referencia (ARS por USD)
 * @param cardDollarRate      cotización usada para pagar en ARS consumos de tarjeta en USD
 * @param payoneerDollarRate  cotización neta al cambiar USD de Payoneer hacia Santander
 * @param conservativeBaseUsd base conservadora en USD sobre la que se arma el plan
 * @param cardMonthlyLimitUsd límite total de consumos con crédito para el mes
 */
public record Income(
        BigDecimal salaryArs,
        BigDecimal salaryUsd,
        BigDecimal referenceRate,
        BigDecimal cardDollarRate,
        BigDecimal payoneerDollarRate,
        BigDecimal conservativeBaseUsd,
        BigDecimal cardMonthlyLimitUsd
) {
    public Income withCardMonthlyLimitUsd(BigDecimal newCardMonthlyLimitUsd) {
        return new Income(salaryArs, salaryUsd, referenceRate, cardDollarRate, payoneerDollarRate,
                conservativeBaseUsd, newCardMonthlyLimitUsd);
    }
}
