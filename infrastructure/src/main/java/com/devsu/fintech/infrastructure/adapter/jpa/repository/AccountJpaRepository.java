package com.devsu.fintech.infrastructure.adapter.jpa.repository;

import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long>,
        JpaSpecificationExecutor<AccountEntity> {

    boolean existsByClientIdAndAccountStatusIdNot(Long clientId, Integer accountStatusId);

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    List<AccountEntity> findByClientId(Long clientId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM AccountEntity a WHERE a.accountId = :accountId")
    Optional<AccountEntity> findByIdWithLock(@Param("accountId") Long accountId);
}
