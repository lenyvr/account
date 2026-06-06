package com.devsu.fintech.infrastructure.adapter.jpa;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.domain.ports.output.TransactionRepositorySPI;
import com.devsu.fintech.infrastructure.adapter.jpa.entity.AccountEntity;
import com.devsu.fintech.infrastructure.adapter.jpa.entity.TransactionEntity;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.AccountJpaRepository;
import com.devsu.fintech.infrastructure.adapter.jpa.repository.TransactionJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class TransactionRepositoryAdapter implements TransactionRepositorySPI {

    private final TransactionJpaRepository transactionJpaRepository;
    private final AccountJpaRepository accountJpaRepository;

    public TransactionRepositoryAdapter(TransactionJpaRepository transactionJpaRepository,
                                        AccountJpaRepository accountJpaRepository) {
        this.transactionJpaRepository = transactionJpaRepository;
        this.accountJpaRepository = accountJpaRepository;
    }

    @Override
    @Transactional
    public Transaction registerTransaction(Transaction transaction, Account updatedAccount) {
        AccountEntity accountEntity = accountJpaRepository
                .findByIdWithLock(updatedAccount.getAccountId())
                .orElseThrow();
        accountEntity.setBalance(updatedAccount.getBalance());
        accountJpaRepository.save(accountEntity);
        TransactionEntity saved = transactionJpaRepository.save(toEntity(transaction));
        return toDomain(saved);
    }

    private TransactionEntity toEntity(Transaction tx) {
        TransactionEntity entity = new TransactionEntity();
        entity.setAccountId(tx.getAccountId());
        entity.setAmount(tx.getAmount());
        entity.setTransactionTypeId(tx.getTransactionTypeId());
        entity.setAccountNumberDestination(tx.getAccountNumberDestination());
        return entity;
    }

    private Transaction toDomain(TransactionEntity entity) {
        Transaction tx = new Transaction();
        tx.setTransactionId(entity.getTransactionId());
        tx.setAmount(entity.getAmount());
        tx.setAccountId(entity.getAccountId());
        tx.setTransactionTypeId(entity.getTransactionTypeId());
        tx.setAccountNumberDestination(entity.getAccountNumberDestination());
        tx.setTransactionDate(entity.getTransactionDate());
        return tx;
    }
}
