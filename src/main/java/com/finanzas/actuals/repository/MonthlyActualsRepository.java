package com.finanzas.actuals.repository;

import java.util.Optional;

import com.finanzas.actuals.model.MonthlyActuals;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MonthlyActualsRepository extends ListCrudRepository<MonthlyActuals, Long> {

    Optional<MonthlyActuals> findByPeriodId(Long periodId);
}
