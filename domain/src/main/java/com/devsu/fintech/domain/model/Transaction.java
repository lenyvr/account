package com.devsu.fintech.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class Transaction {

    private Long transactionId;
    private BigDecimal amount;
    private String accountNumberDestination;
    private Integer transactionTypeId;
    private Long accountId;
    private OffsetDateTime transactionDate;

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getAccountNumberDestination() { return accountNumberDestination; }
    public void setAccountNumberDestination(String accountNumberDestination) {
        this.accountNumberDestination = accountNumberDestination;
    }

    public Integer getTransactionTypeId() { return transactionTypeId; }
    public void setTransactionTypeId(Integer transactionTypeId) { this.transactionTypeId = transactionTypeId; }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public OffsetDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(OffsetDateTime transactionDate) { this.transactionDate = transactionDate; }
}
