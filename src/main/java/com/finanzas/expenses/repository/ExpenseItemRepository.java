package com.finanzas.expenses.repository;

import java.util.List;

import com.finanzas.expenses.model.ExpenseItem;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExpenseItemRepository extends ListCrudRepository<ExpenseItem, Long> {

    List<ExpenseItem> findByPeriodIdOrderBySortOrderAscIdAsc(Long periodId);
}
