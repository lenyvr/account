package com.devsu.fintech.infrastructure.adapter.jpa;

import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountJpaRepository;
import org.springframework.stereotype.Component;

@Component
public class AccountRepositoryAdapter implements AccountRepositorySPI {

    private static final int CLOSED_STATUS_ID = 5;

    private final AccountJpaRepository jpaRepository;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean hasOpenedAccounts(Integer clientId) {
        return jpaRepository.existsByClientIdAndAccountStatusIdNot(clientId, CLOSED_STATUS_ID);
    }
}
