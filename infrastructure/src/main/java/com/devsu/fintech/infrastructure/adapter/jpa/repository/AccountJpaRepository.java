package com.devsu.fintech.infrastructure.adapter.jpa.repository;

import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    boolean existsByClientIdAndAccountStatusIdNot(Long clientId, Integer accountStatusId);

    Optional<AccountEntity> findByAccountNumber(String accountNumber);
}
