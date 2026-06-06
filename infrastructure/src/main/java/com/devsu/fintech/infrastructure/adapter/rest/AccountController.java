package com.devsu.fintech.infrastructure.adapter.rest;

import com.devsu.fintech.application.port.input.CreateAccountInputPort;
import com.devsu.fintech.application.port.input.ListAccountsInputPort;
import com.devsu.fintech.application.port.input.UpdateAccountInputPort;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.domain.model.AccountFilter;
import com.devsu.fintech.domain.model.AccountPage;
import com.devsu.fintech.infrastructure.adapter.rest.dto.AccountListItemDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.CreateAccountRequestDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.CreateAccountResponseDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.PagedResponseDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.UpdateAccountRequestDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.UpdateAccountResponseDTO;
import com.devsu.fintech.infrastructure.adapter.rest.mapper.AccountMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Account management")
public class AccountController {

    private final CreateAccountInputPort createAccountInputPort;
    private final UpdateAccountInputPort updateAccountInputPort;
    private final ListAccountsInputPort listAccountsInputPort;

    public AccountController(CreateAccountInputPort createAccountInputPort,
                             UpdateAccountInputPort updateAccountInputPort,
                             ListAccountsInputPort listAccountsInputPort) {
        this.createAccountInputPort = createAccountInputPort;
        this.updateAccountInputPort = updateAccountInputPort;
        this.listAccountsInputPort = listAccountsInputPort;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "List non-closed accounts with optional filters and pagination")
    @ApiResponse(responseCode = "200", description = "Paginated list of accounts")
    public PagedResponseDTO<AccountListItemDTO> listAccounts(
            @RequestParam(required = false) String accountNumber,
            @RequestParam(required = false) String accountType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate createdDate,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(required = false) BigDecimal initialBalance,
            @RequestParam(required = false) BigDecimal finalBalance,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        AccountFilter filter = new AccountFilter(accountNumber, accountType, createdDate,
                accountStatus, initialBalance, finalBalance);
        AccountPage result = listAccountsInputPort.execute(filter, page, size);

        List<AccountListItemDTO> content = result.getAccounts().stream()
                .map(AccountMapper::toListItemDTO)
                .toList();

        return new PagedResponseDTO<>(content, result.getTotalElements(),
                result.getTotalPages(), result.getCurrentPage(), result.getPageSize());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new bank account")
    @ApiResponse(responseCode = "201", description = "Account created successfully")
    @ApiResponse(responseCode = "422", description = "Client not found or not verified")
    @ApiResponse(responseCode = "409", description = "Account number already exists")
    public CreateAccountResponseDTO createAccount(@RequestBody CreateAccountRequestDTO request) {
        Account account = AccountMapper.toDomain(request);
        Account created = createAccountInputPort.execute(account);
        return AccountMapper.toResponseDTO(created);
    }

    @PatchMapping("/{accountNumber}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update an existing account's status or expiry date")
    @ApiResponse(responseCode = "200", description = "Account updated successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "422", description = "Business rule violation")
    public UpdateAccountResponseDTO updateAccount(@PathVariable String accountNumber,
                                                  @RequestBody UpdateAccountRequestDTO request) {
        Account updated = updateAccountInputPort.execute(
                accountNumber, request.accountStatusId(), request.expiryDepositDate());
        return AccountMapper.toUpdateResponseDTO(updated);
    }
}
