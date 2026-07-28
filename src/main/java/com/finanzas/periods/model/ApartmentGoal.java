package com.finanzas.periods.model;

import java.math.BigDecimal;

/**
 * Hoja "Apartamento" del plan original.
 *
 * @param targetPriceUsd      precio objetivo del apartamento
 * @param downPaymentPercent  porcentaje necesario para entrada y gastos (0..1)
 * @param currentSavingsUsd   ahorros ya disponibles para la meta
 */
public record ApartmentGoal(
        BigDecimal targetPriceUsd,
        BigDecimal downPaymentPercent,
        BigDecimal currentSavingsUsd
) {
}
