package com.devsu.fintech.infrastructure.adapter.rest.mapper;

import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.DeactivationResult;
import com.devsu.fintech.infrastructure.adapter.rest.dto.AccountListItemDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.CreateAccountRequestDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.CreateAccountResponseDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.DeactivateAccountResponseDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.UpdateAccountResponseDTO;

public class AccountMapper {

    private AccountMapper() {}

    public static Account toDomain(CreateAccountRequestDTO dto) {
        Account account = new Account();
        account.setAccountNumber(dto.accountNumber());
        account.setBalance(dto.balance());
        account.setClientId(dto.clientId());
        account.setAccountTypeId(dto.accountTypeId());
        account.setExpiryDepositDate(dto.expiryDepositDate());
        return account;
    }

    public static CreateAccountResponseDTO toResponseDTO(Account account) {
        return new CreateAccountResponseDTO(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getInitialAmount(),
                account.getBalance(),
                account.getAccountStatusId(),
                account.getClientId(),
                account.getAccountTypeId(),
                account.getExpiryDepositDate(),
                account.getCreatedDate()
        );
    }

    public static AccountListItemDTO toListItemDTO(Account account) {
        return new AccountListItemDTO(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getInitialAmount(),
                account.getBalance(),
                account.getAccountStatusName(),
                account.getClientId(),
                account.getAccountTypeName(),
                account.getExpiryDepositDate(),
                account.getCreatedDate()
        );
    }

    public static DeactivateAccountResponseDTO toDeactivateResponseDTO(DeactivationResult result) {
        Account account = result.account();
        return new DeactivateAccountResponseDTO(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getAccountStatusId(),
                account.getBalance(),
                result.refundedAmount(),
                account.getLastStatusDate()
        );
    }

    public static UpdateAccountResponseDTO toUpdateResponseDTO(Account account) {
        return new UpdateAccountResponseDTO(
                account.getAccountId(),
                account.getAccountNumber(),
                account.getInitialAmount(),
                account.getBalance(),
                account.getAccountStatusId(),
                account.getClientId(),
                account.getAccountTypeId(),
                account.getExpiryDepositDate(),
                account.getCreatedDate(),
                account.getLastStatusDate(),
                account.getLastChangeDate()
        );
    }
}
