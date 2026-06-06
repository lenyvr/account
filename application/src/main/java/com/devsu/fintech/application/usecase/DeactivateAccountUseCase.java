package com.devsu.fintech.application.usecase;

import com.devsu.fintech.application.port.input.DeactivateAccountInputPort;
import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.InvalidRefundMethodException;
import com.devsu.fintech.domain.exception.TargetAccountRequiredException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.DeactivationResult;
import com.devsu.fintech.domain.model.RefundMethod;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;

import java.math.BigDecimal;

public class DeactivateAccountUseCase implements DeactivateAccountInputPort {

    private static final Integer CLOSED_STATUS_ID = 5;
    private static final Integer CASH_WITHDRAWAL_TYPE_ID = 3;
    private static final Integer TRANSFER_OUTBOUND_TYPE_ID = 4;

    private final AccountRepositorySPI accountRepositorySPI;

    public DeactivateAccountUseCase(AccountRepositorySPI accountRepositorySPI) {
        this.accountRepositorySPI = accountRepositorySPI;
    }

    @Override
    public DeactivationResult execute(String accountNumber, String refundMethodStr, String targetAccountNumber) {
        Account account = accountRepositorySPI.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (CLOSED_STATUS_ID.equals(account.getAccountStatusId())) {
            throw new AccountClosedException(accountNumber);
        }

        RefundMethod refundMethod = RefundMethod.fromString(refundMethodStr)
                .orElseThrow(() -> new InvalidRefundMethodException(refundMethodStr));

        if (RefundMethod.TRANSFER.equals(refundMethod)
                && (targetAccountNumber == null || targetAccountNumber.isBlank())) {
            throw new TargetAccountRequiredException();
        }

        BigDecimal refundedAmount = account.getBalance();
        Transaction refundTransaction = null;

        if (refundedAmount.compareTo(BigDecimal.ZERO) > 0) {
            refundTransaction = buildRefundTransaction(account, refundMethod, targetAccountNumber, refundedAmount);
            account.setBalance(BigDecimal.ZERO);
        }

        account.setAccountStatusId(CLOSED_STATUS_ID);

        Account deactivated = accountRepositorySPI.deactivate(account, refundTransaction);
        return new DeactivationResult(deactivated, refundedAmount);
    }

    private Transaction buildRefundTransaction(Account account, RefundMethod method,
                                               String targetAccountNumber, BigDecimal balance) {
        Transaction tx = new Transaction();
        tx.setAccountId(account.getAccountId());
        tx.setAmount(balance.negate());
        tx.setTransactionTypeId(RefundMethod.TRANSFER.equals(method)
                ? TRANSFER_OUTBOUND_TYPE_ID : CASH_WITHDRAWAL_TYPE_ID);
        tx.setAccountNumberDestination(RefundMethod.TRANSFER.equals(method) ? targetAccountNumber : null);
        return tx;
    }
}
