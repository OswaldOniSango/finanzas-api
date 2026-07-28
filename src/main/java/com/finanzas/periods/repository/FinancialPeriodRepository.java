package com.finanzas.periods.repository;

import java.util.List;
import java.util.Optional;

import com.finanzas.periods.model.FinancialPeriod;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialPeriodRepository extends ListCrudRepository<FinancialPeriod, Long> {

    List<FinancialPeriod> findAllByOrderByPeriodYearDescPeriodMonthDesc();

    List<FinancialPeriod> findAllByOrderByPeriodYearAscPeriodMonthAsc();

    Optional<FinancialPeriod> findByPeriodYearAndPeriodMonth(int periodYear, int periodMonth);

    Optional<FinancialPeriod> findFirstByOrderByPeriodYearDescPeriodMonthDesc();
}
