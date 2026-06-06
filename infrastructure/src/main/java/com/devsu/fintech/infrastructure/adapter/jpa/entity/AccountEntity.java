package com.devsu.fintech.infrastructure.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "account")
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "initial_amount", nullable = false)
    private BigDecimal initialAmount;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance;

    @Column(name = "account_status_id", nullable = false)
    private Integer accountStatusId;

    @Column(name = "client_id", nullable = false)
    private Integer clientId;

    @Column(name = "account_type_id", nullable = false)
    private Integer accountTypeId;

    @Column(name = "expiry_deposit_date")
    private LocalDate expiryDepositDate;

    @Column(name = "created_date", insertable = false, updatable = false)
    private OffsetDateTime createdDate;

    @Column(name = "last_status_date", insertable = false, updatable = false)
    private OffsetDateTime lastStatusDate;

    @Column(name = "last_change_date", insertable = false, updatable = false)
    private OffsetDateTime lastChangeDate;

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }

    public BigDecimal getInitialAmount() { return initialAmount; }
    public void setInitialAmount(BigDecimal initialAmount) { this.initialAmount = initialAmount; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public Integer getAccountStatusId() { return accountStatusId; }
    public void setAccountStatusId(Integer accountStatusId) { this.accountStatusId = accountStatusId; }

    public Integer getClientId() { return clientId; }
    public void setClientId(Integer clientId) { this.clientId = clientId; }

    public Integer getAccountTypeId() { return accountTypeId; }
    public void setAccountTypeId(Integer accountTypeId) { this.accountTypeId = accountTypeId; }

    public LocalDate getExpiryDepositDate() { return expiryDepositDate; }
    public void setExpiryDepositDate(LocalDate expiryDepositDate) { this.expiryDepositDate = expiryDepositDate; }

    public OffsetDateTime getCreatedDate() { return createdDate; }
    public OffsetDateTime getLastStatusDate() { return lastStatusDate; }
    public OffsetDateTime getLastChangeDate() { return lastChangeDate; }
}
