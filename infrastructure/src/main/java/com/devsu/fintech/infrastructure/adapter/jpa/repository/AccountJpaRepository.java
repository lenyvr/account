package com.devsu.fintech.infrastructure.adapter.jpa.repository;

import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    boolean existsByClientIdAndAccountStatusIdNot(Integer clientId, Integer accountStatusId);
}
