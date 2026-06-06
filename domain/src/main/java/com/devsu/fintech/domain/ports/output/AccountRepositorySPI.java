package com.devsu.fintech.domain.ports.output;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.AccountFilter;
import com.devsu.fintech.domain.model.AccountPage;
import com.devsu.fintech.domain.model.Transaction;
import java.util.List;
import java.util.Optional;

public interface AccountRepositorySPI {

    boolean hasOpenedAccounts(Long clientId);

    Account save(Account account);

    Optional<Account> findByAccountNumber(String accountNumber);

    Account update(Account account);

    AccountPage listAccounts(AccountFilter filter, int page, int size);

    Account deactivate(Account account, Transaction refundTransaction);

    Optional<Account> findById(Long accountId);

    List<Account> findByClientId(Long clientId);
}
