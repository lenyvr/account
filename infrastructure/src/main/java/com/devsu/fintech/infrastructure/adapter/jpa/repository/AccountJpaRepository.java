package com.devsu.fintech.infrastructure.adapter.jpa.repository;

import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long>,
        JpaSpecificationExecutor<AccountEntity> {

    boolean existsByClientIdAndAccountStatusIdNot(Long clientId, Integer accountStatusId);

    Optional<AccountEntity> findByAccountNumber(String accountNumber);
}
