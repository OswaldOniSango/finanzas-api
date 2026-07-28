package com.finanzas.calculator.model;

import java.math.BigDecimal;

/**
 * Un punto de la curva de ahorro (filas 14 a 50 del Excel).
 *
 * <p>Sólo lleva lo que la curva dibuja. El ahorro mensual es constante en toda la
 * serie y el avance se deriva contra la meta, así que ambos viven en
 * {@link ApartmentSummary} en lugar de repetirse en cada punto.
 *
 * @param month mes relativo, 0 = hoy
 */
public record ProjectionPoint(
        int month,
        String label,
        BigDecimal accumulatedUsd
) {
}
