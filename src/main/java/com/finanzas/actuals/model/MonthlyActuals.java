package com.finanzas.actuals.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

@Table("monthly_actuals")
public record MonthlyActuals(
        @Id Long id,
        Long periodId,
        BigDecimal usdExchanged,
        BigDecimal arsReceived,
        BigDecimal cardPaymentsArs,
        BigDecimal cardPaymentsUsd,
        String notes,
        @CreatedDate LocalDateTime createdAt,
        @LastModifiedDate LocalDateTime updatedAt
) {
}
