package com.devsu.fintech.application.usecase;

import com.devsu.fintech.application.port.input.UpdateAccountInputPort;
import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountClosureNotAllowedViaUpdateException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.InvalidDormantTransitionException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public class UpdateAccountUseCase implements UpdateAccountInputPort {

    private static final Integer CLOSED_STATUS_ID = 5;
    private static final Integer DORMANT_STATUS_ID = 4;

    private final AccountRepositorySPI accountRepositorySPI;

    public UpdateAccountUseCase(AccountRepositorySPI accountRepositorySPI) {
        this.accountRepositorySPI = accountRepositorySPI;
    }

    @Override
    public Account execute(String accountNumber, Integer newStatusId, LocalDate newExpiryDate) {
        Account account = accountRepositorySPI.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        if (CLOSED_STATUS_ID.equals(account.getAccountStatusId())) {
            throw new AccountClosedException(accountNumber);
        }

        if (newStatusId != null) {
            if (CLOSED_STATUS_ID.equals(newStatusId)) {
                throw new AccountClosureNotAllowedViaUpdateException();
            }
            if (DORMANT_STATUS_ID.equals(newStatusId)) {
                validateDormantTransition(account);
            }
            account.setAccountStatusId(newStatusId);
        }

        if (newExpiryDate != null) {
            account.setExpiryDepositDate(newExpiryDate);
        }

        return accountRepositorySPI.update(account);
    }

    private void validateDormantTransition(Account account) {
        boolean inactiveForSixMonths = account.getLastChangeDate() != null
                && account.getLastChangeDate().isBefore(OffsetDateTime.now().minusMonths(6));
        boolean hasZeroBalance = BigDecimal.ZERO.compareTo(account.getBalance()) == 0;

        if (!inactiveForSixMonths || !hasZeroBalance) {
            throw new InvalidDormantTransitionException();
        }
    }
}
