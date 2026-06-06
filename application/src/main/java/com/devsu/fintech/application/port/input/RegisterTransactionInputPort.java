package com.devsu.fintech.application.port.input;

import com.devsu.fintech.domain.model.Transaction;

public interface RegisterTransactionInputPort {

    Transaction execute(Transaction transaction);
}
