package com.devsu.fintech.domain.ports.output;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.Transaction;

public interface TransactionRepositorySPI {

    Transaction registerTransaction(Transaction transaction, Account updatedAccount);
}
