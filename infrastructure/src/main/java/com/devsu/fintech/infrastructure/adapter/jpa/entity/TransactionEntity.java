package com.devsu.fintech.infrastructure.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transaction")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "transaction_date", insertable = false, updatable = false)
    private OffsetDateTime transactionDate;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "account_number_destination")
    private String accountNumberDestination;

    @Column(name = "transaction_type_id", nullable = false)
    private Integer transactionTypeId;

    @Column(name = "account_id")
    private Long accountId;

    public Long getTransactionId() { return transactionId; }
    public void setTransactionId(Long transactionId) { this.transactionId = transactionId; }

    public OffsetDateTime getTransactionDate() { return transactionDate; }

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
}
