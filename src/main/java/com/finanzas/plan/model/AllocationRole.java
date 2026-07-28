package com.finanzas.plan.model;

/**
 * Marca qué línea del plan alimenta a otros cálculos. En el Excel esto estaba
 * implícito en referencias fijas como {@code 'Plan mensual'!C4}; acá es explícito
 * para que renombrar una línea no rompa las fórmulas.
 */
public enum AllocationRole {

    /** Línea informativa: sólo aporta a los totales de su etapa. */
    NONE,

    /** Presupuesto objetivo contra el que se comparan los gastos reales. */
    PRESUPUESTO_GASTOS,

    /** Meta de ahorro mensual destinada al apartamento. */
    AHORRO_APARTAMENTO
}
