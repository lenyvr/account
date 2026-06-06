package com.devsu.fintech.infrastructure.adapter.jpa.repository;

import com.devsu.fintech.infrastructure.adapter.jpa.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.OffsetDateTime;
import java.util.List;

public interface TransactionJpaRepository extends JpaRepository<TransactionEntity, Long> {

    @Query("SELECT t FROM TransactionEntity t WHERE t.accountId IN :accountIds AND t.transactionDate >= :from AND t.transactionDate < :to ORDER BY t.transactionDate ASC")
    List<TransactionEntity> findTransactionsForReport(@Param("accountIds") List<Long> accountIds,
                                                      @Param("from") OffsetDateTime from,
                                                      @Param("to") OffsetDateTime to);
}
