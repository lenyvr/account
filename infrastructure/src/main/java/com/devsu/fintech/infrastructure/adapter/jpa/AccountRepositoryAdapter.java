package com.devsu.fintech.infrastructure.adapter.jpa;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountRepositoryAdapter implements AccountRepositorySPI {

    private static final int CLOSED_STATUS_ID = 5;

    private final AccountJpaRepository jpaRepository;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public boolean hasOpenedAccounts(Long clientId) {
        return jpaRepository.existsByClientIdAndAccountStatusIdNot(clientId, CLOSED_STATUS_ID);
    }

    @Override
    public Account save(Account account) {
        AccountEntity saved = jpaRepository.save(toEntity(account));
        return toDomain(saved);
    }

    @Override
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return jpaRepository.findByAccountNumber(accountNumber).map(this::toDomain);
    }

    @Override
    public Account update(Account account) {
        AccountEntity saved = jpaRepository.save(toEntity(account));
        return toDomain(saved);
    }

    private AccountEntity toEntity(Account account) {
        AccountEntity entity = new AccountEntity();
        entity.setAccountId(account.getAccountId());
        entity.setAccountNumber(account.getAccountNumber());
        entity.setInitialAmount(account.getInitialAmount());
        entity.setBalance(account.getBalance());
        entity.setAccountStatusId(account.getAccountStatusId());
        entity.setClientId(account.getClientId());
        entity.setAccountTypeId(account.getAccountTypeId());
        entity.setExpiryDepositDate(account.getExpiryDepositDate());
        return entity;
    }

    private Account toDomain(AccountEntity entity) {
        Account account = new Account();
        account.setAccountId(entity.getAccountId());
        account.setAccountNumber(entity.getAccountNumber());
        account.setInitialAmount(entity.getInitialAmount());
        account.setBalance(entity.getBalance());
        account.setAccountStatusId(entity.getAccountStatusId());
        account.setClientId(entity.getClientId());
        account.setAccountTypeId(entity.getAccountTypeId());
        account.setExpiryDepositDate(entity.getExpiryDepositDate());
        account.setCreatedDate(entity.getCreatedDate());
        account.setLastStatusDate(entity.getLastStatusDate());
        account.setLastChangeDate(entity.getLastChangeDate());
        return account;
    }
}
