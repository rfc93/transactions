package com.rfc.transactions.repository;

import com.rfc.transactions.model.entity.TransactionsEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionsRepository extends JpaRepository<TransactionsEntity, Integer> {

    List<TransactionsEntity> findByAccountIBAN(String accountIBAN, Sort sort);

    Optional<TransactionsEntity> findByReference(String reference);
}
