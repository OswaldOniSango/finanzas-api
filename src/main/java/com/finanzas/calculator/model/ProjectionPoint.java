package com.finanzas.calculator.model;

import java.math.BigDecimal;

/**
 * Un punto de la tabla de proyección del apartamento (filas 14 a 50 del Excel).
 *
 * @param month mes relativo, 0 = hoy
 */
public record ProjectionPoint(
        int month,
        String label,
        BigDecimal monthlySavingUsd,
        BigDecimal accumulatedUsd,
        BigDecimal goalProgress
) {
}
