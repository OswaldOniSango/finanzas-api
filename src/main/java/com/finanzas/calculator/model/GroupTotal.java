package com.finanzas.calculator.model;

import java.math.BigDecimal;

/**
 * Total agrupado, usado por el gráfico de distribución de gastos.
 */
public record GroupTotal(
        String label,
        BigDecimal amountArs,
        BigDecimal amountUsd,
        BigDecimal share
) {
}
