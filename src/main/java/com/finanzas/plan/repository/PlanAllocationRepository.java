package com.finanzas.plan.repository;

import java.util.List;

import com.finanzas.plan.model.PlanAllocation;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlanAllocationRepository extends ListCrudRepository<PlanAllocation, Long> {

    List<PlanAllocation> findByPeriodIdOrderByStageAscSortOrderAscIdAsc(Long periodId);
}
