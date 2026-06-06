package com.devsu.fintech.infrastructure.adapter.rest.mapper;

import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.infrastructure.adapter.rest.dto.RegisterTransactionRequestDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.RegisterTransactionResponseDTO;

public class TransactionMapper {
    public TransactionMapper() {
    }

    public static Transaction toTransaction(RegisterTransactionRequestDTO dto) {
        Transaction tx = new Transaction();
        tx.setAccountId(dto.accountId());
        tx.setAmount(dto.amount());
        tx.setTransactionTypeId(dto.transactionType().getId());
        tx.setAccountNumberDestination(dto.accountNumberDestination());
        return tx;
    }

    public static RegisterTransactionResponseDTO toResponseDTO(Transaction tx) {
        return new RegisterTransactionResponseDTO(
                tx.getTransactionId(),
                tx.getAccountId(),
                tx.getAmount(),
                tx.getTransactionTypeId(),
                tx.getAccountNumberDestination(),
                tx.getTransactionDate()
        );
    }
}
