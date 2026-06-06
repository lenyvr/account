package com.devsu.fintech.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Account {

    private Long accountId;
    private String accountNumber;
    private BigDecimal initialAmount;
    private BigDecimal balance;
    private Integer accountStatusId;
    private Long clientId;
    private Integer accountTypeId;
    private LocalDate expiryDepositDate;
    private LocalDate createdDate;
    private LocalDate lastStatusDate;
    private LocalDate lastChangeDate;

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

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public Integer getAccountTypeId() { return accountTypeId; }
    public void setAccountTypeId(Integer accountTypeId) { this.accountTypeId = accountTypeId; }

    public LocalDate getExpiryDepositDate() { return expiryDepositDate; }
    public void setExpiryDepositDate(LocalDate expiryDepositDate) { this.expiryDepositDate = expiryDepositDate; }

    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }

    public LocalDate getLastStatusDate() { return lastStatusDate; }
    public void setLastStatusDate(LocalDate lastStatusDate) { this.lastStatusDate = lastStatusDate; }

    public LocalDate getLastChangeDate() { return lastChangeDate; }
    public void setLastChangeDate(LocalDate lastChangeDate) { this.lastChangeDate = lastChangeDate; }
}
