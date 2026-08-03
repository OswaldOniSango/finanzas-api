package com.finanzas.actuals.dto;

import java.math.BigDecimal;

import com.finanzas.actuals.model.MonthlyActuals;

public record MonthlyActualsResponse(
        BigDecimal actualPayoneerRate,
        BigDecimal usdExchanged,
        BigDecimal arsReceived,
        BigDecimal cardPaymentsArs,
        BigDecimal cardPaymentsUsd,
        String notes
) {
    public static MonthlyActualsResponse empty() {
        return new MonthlyActualsResponse(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, null);
    }

    public static MonthlyActualsResponse from(MonthlyActuals actuals) {
        return new MonthlyActualsResponse(
                actuals.actualPayoneerRate(), actuals.usdExchanged(),
                actuals.arsReceived(), actuals.cardPaymentsArs(),
                actuals.cardPaymentsUsd(), actuals.notes());
    }
}
