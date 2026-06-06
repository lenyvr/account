package com.devsu.fintech.application.usecase;

import com.devsu.fintech.application.port.input.RegisterTransactionInputPort;
import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.InsufficientFundsException;
import com.devsu.fintech.domain.exception.TargetAccountRequiredException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.RefundMethod;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.domain.model.TransactionType;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.domain.ports.output.TransactionRepositorySPI;

import java.math.BigDecimal;
import java.util.Objects;

public class RegisterTransactionUseCase implements RegisterTransactionInputPort {

    private static final Integer CLOSED_STATUS_ID = 5;

    private final AccountRepositorySPI accountRepositorySPI;
    private final TransactionRepositorySPI transactionRepositorySPI;

    public RegisterTransactionUseCase(AccountRepositorySPI accountRepositorySPI,
                                      TransactionRepositorySPI transactionRepositorySPI) {
        this.accountRepositorySPI = accountRepositorySPI;
        this.transactionRepositorySPI = transactionRepositorySPI;
    }

    @Override
    public Transaction execute(Transaction transaction) {
        validateAmount(transaction);
        validateTransactionType(transaction);
        Account account = validateAccount(transaction);
        return transactionRepositorySPI.registerTransaction(transaction, account);
    }

    private void validateTransactionType(Transaction transaction) {
        if (TransactionType.TRANSFER_OUTBOUND.getId().equals(transaction.getTransactionTypeId())
                && (transaction.getAccountNumberDestination() == null
                || transaction.getAccountNumberDestination().isBlank())) {
            throw new TargetAccountRequiredException();
        }
    }

    private void validateAmount(Transaction transaction) {
        if(transaction.getAmount() == null ||
                BigDecimal.ZERO.compareTo(transaction.getAmount()) >= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        if(TransactionType.CASH_WITHDRAWAL.getId().equals(transaction.getTransactionTypeId())
        || TransactionType.TRANSFER_OUTBOUND.getId().equals(transaction.getTransactionTypeId())){
            transaction.setAmount(transaction.getAmount().negate());
        }
    }


    private Account validateAccount(Transaction transaction){
        Account account = accountRepositorySPI.findById(transaction.getAccountId())
                .orElseThrow(() -> new AccountNotFoundException(
                        "ID: " + transaction.getAccountId()));

        if (CLOSED_STATUS_ID.equals(account.getAccountStatusId())) {
            throw new AccountClosedException(account.getAccountNumber());
        }

        BigDecimal newBalance = account.getBalance().add(transaction.getAmount());
        if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException();
        }

        account.setBalance(newBalance);
        return account;
    }
}
