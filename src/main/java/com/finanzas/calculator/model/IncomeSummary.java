package com.finanzas.calculator.model;

import java.math.BigDecimal;

/**
 * Equivale a las filas calculadas de la hoja "Ingresos".
 *
 * @param salaryUsdInArs   sueldo en dólares llevado a pesos (B5 * B6)
 * @param totalIncomeArs   ingreso total mensual en pesos (B4 + B7)
 * @param totalIncomeUsd   ingreso total equivalente en dólares (B8 / B6)
 */
public record IncomeSummary(
        BigDecimal salaryArs,
        BigDecimal salaryUsd,
        BigDecimal referenceRate,
        BigDecimal cardDollarRate,
        BigDecimal payoneerDollarRate,
        BigDecimal salaryUsdInArs,
        BigDecimal totalIncomeArs,
        BigDecimal totalIncomeUsd,
        BigDecimal conservativeBaseUsd,
        BigDecimal conservativeBaseArs
) {
}
