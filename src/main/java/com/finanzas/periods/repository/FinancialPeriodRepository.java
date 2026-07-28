package com.finanzas.periods.repository;

import java.util.List;
import java.util.Optional;

import com.finanzas.periods.model.FinancialPeriod;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialPeriodRepository extends ListCrudRepository<FinancialPeriod, Long> {

    List<FinancialPeriod> findAllByOwnerUserIdOrderByPeriodYearDescPeriodMonthDesc(Long ownerUserId);

    List<FinancialPeriod> findAllByOwnerUserIdOrderByPeriodYearAscPeriodMonthAsc(Long ownerUserId);

    Optional<FinancialPeriod> findByOwnerUserIdAndPeriodYearAndPeriodMonth(
            Long ownerUserId, int periodYear, int periodMonth);

    Optional<FinancialPeriod> findFirstByOwnerUserIdOrderByPeriodYearDescPeriodMonthDesc(Long ownerUserId);

    Optional<FinancialPeriod> findByIdAndOwnerUserId(Long id, Long ownerUserId);
}
