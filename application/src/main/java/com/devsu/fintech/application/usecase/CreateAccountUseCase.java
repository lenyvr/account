package com.devsu.fintech.application.usecase;

import com.devsu.fintech.application.port.input.CreateAccountInputPort;
import com.devsu.fintech.domain.exception.ClientNotFoundException;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.ports.output.AccountRepositorySPI;
import com.devsu.fintech.domain.ports.output.ClientVerificationSPI;

public class CreateAccountUseCase implements CreateAccountInputPort {

    private static final Integer PENDING_ACTIVATION_STATUS_ID = 1;
    private static final Integer TIME_DEPOSIT_TYPE_ID = 3;

    private final AccountRepositorySPI accountRepositorySPI;
    private final ClientVerificationSPI clientVerificationSPI;

    public CreateAccountUseCase(AccountRepositorySPI accountRepositorySPI,
                                ClientVerificationSPI clientVerificationSPI) {
        this.accountRepositorySPI = accountRepositorySPI;
        this.clientVerificationSPI = clientVerificationSPI;
    }

    @Override
    public Account execute(Account account) {
        if (!clientVerificationSPI.existsClient(account.getClientId())) {
            throw new ClientNotFoundException(account.getClientId());
        }

        account.setAccountStatusId(PENDING_ACTIVATION_STATUS_ID);
        account.setInitialAmount(account.getBalance());

        if (!TIME_DEPOSIT_TYPE_ID.equals(account.getAccountTypeId())) {
            account.setExpiryDepositDate(null);
        }

        return accountRepositorySPI.save(account);
    }
}
