package com.devsu.fintech.domain.ports.output;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.Transaction;
import java.time.OffsetDateTime;
import java.util.List;

public interface TransactionRepositorySPI {

    Transaction registerTransaction(Transaction transaction, Account updatedAccount);

    List<Transaction> findTransactionsForReport(List<Long> accountIds, OffsetDateTime from, OffsetDateTime to);
}
