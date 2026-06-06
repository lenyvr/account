package com.devsu.fintech.infrastructure.adapter.jpa;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.AccountFilter;
import com.devsu.fintech.domain.model.AccountPage;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountStatusEntity;
import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountTypeEntity;
import com.devsu.fintech.infrastructure.adapter.jpa.entity.TransactionEntity;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountJpaRepository;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountStatusJpaRepository;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountTypeJpaRepository;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.TransactionJpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class AccountRepositoryAdapter implements AccountRepositorySPI {

    private static final int CLOSED_STATUS_ID = 5;

    private final AccountJpaRepository jpaRepository;
    private final AccountTypeJpaRepository accountTypeRepository;
    private final AccountStatusJpaRepository accountStatusRepository;
    private final TransactionJpaRepository transactionJpaRepository;

    public AccountRepositoryAdapter(AccountJpaRepository jpaRepository,
                                    AccountTypeJpaRepository accountTypeRepository,
                                    AccountStatusJpaRepository accountStatusRepository,
                                    TransactionJpaRepository transactionJpaRepository) {
        this.jpaRepository = jpaRepository;
        this.accountTypeRepository = accountTypeRepository;
        this.accountStatusRepository = accountStatusRepository;
        this.transactionJpaRepository = transactionJpaRepository;
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

    @Override
    public AccountPage listAccounts(AccountFilter filter, int page, int size) {
        Map<Integer, String> typeNames = accountTypeRepository.findAll().stream()
                .collect(Collectors.toMap(AccountTypeEntity::getAccountTypeId, AccountTypeEntity::getName));
        Map<Integer, String> statusNames = accountStatusRepository.findAll().stream()
                .collect(Collectors.toMap(AccountStatusEntity::getAccountStatusId, AccountStatusEntity::getName));

        Specification<AccountEntity> spec = AccountSpecification.excludeClosed();

        if (filter.accountNumber() != null) {
            spec = spec.and(AccountSpecification.accountNumberEquals(filter.accountNumber()));
        }
        if (filter.accountType() != null) {
            Integer typeId = resolveId(typeNames, filter.accountType());
            if (typeId != null) spec = spec.and(AccountSpecification.typeIdEquals(typeId));
        }
        if (filter.accountStatus() != null) {
            Integer statusId = resolveId(statusNames, filter.accountStatus());
            if (statusId != null) spec = spec.and(AccountSpecification.statusIdEquals(statusId));
        }
        if (filter.createdDate() != null) {
            spec = spec.and(AccountSpecification.createdOnDate(filter.createdDate()));
        }
        if (filter.initialBalance() != null) {
            spec = spec.and(AccountSpecification.balanceGreaterThanOrEqual(filter.initialBalance()));
        }
        if (filter.finalBalance() != null) {
            spec = spec.and(AccountSpecification.balanceLessThanOrEqual(filter.finalBalance()));
        }

        Page<AccountEntity> resultPage = jpaRepository.findAll(spec, PageRequest.of(page, size));
        java.util.List<Account> accounts = resultPage.getContent().stream()
                .map(entity -> toDomainWithNames(entity, typeNames, statusNames))
                .collect(Collectors.toList());

        return new AccountPage(accounts, resultPage.getTotalElements(),
                resultPage.getTotalPages(), resultPage.getNumber(), size);
    }

    @Override
    @Transactional
    public Account deactivate(Account account, Transaction refundTransaction) {
        if (refundTransaction != null) {
            transactionJpaRepository.save(toTransactionEntity(refundTransaction));
        }
        AccountEntity saved = jpaRepository.save(toEntity(account));
        return toDomain(saved);
    }

    private TransactionEntity toTransactionEntity(Transaction tx) {
        TransactionEntity entity = new TransactionEntity();
        entity.setAccountId(tx.getAccountId());
        entity.setAmount(tx.getAmount());
        entity.setTransactionTypeId(tx.getTransactionTypeId());
        entity.setAccountNumberDestination(tx.getAccountNumberDestination());
        return entity;
    }

    private Integer resolveId(Map<Integer, String> nameMap, String name) {
        return nameMap.entrySet().stream()
                .filter(e -> e.getValue().equalsIgnoreCase(name))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private Account toDomainWithNames(AccountEntity entity,
                                      Map<Integer, String> typeNames,
                                      Map<Integer, String> statusNames) {
        Account account = toDomain(entity);
        account.setAccountTypeName(typeNames.get(entity.getAccountTypeId()));
        account.setAccountStatusName(statusNames.get(entity.getAccountStatusId()));
        return account;
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
