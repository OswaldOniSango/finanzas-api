package com.finanzas.cards.repository;

import java.util.List;

import com.finanzas.cards.model.CreditCard;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditCardRepository extends ListCrudRepository<CreditCard, Long> {

    List<CreditCard> findByPeriodIdOrderBySortOrderAscIdAsc(Long periodId);
}
