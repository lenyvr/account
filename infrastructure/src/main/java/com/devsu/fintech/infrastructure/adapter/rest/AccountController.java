package com.devsu.fintech.infrastructure.adapter.rest;

import com.devsu.fintech.application.port.input.CreateAccountInputPort;
import com.devsu.fintech.domain.model.Account;
import com.devsu.fintech.infrastructure.adapter.rest.dto.CreateAccountRequestDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.CreateAccountResponseDTO;
import com.devsu.fintech.infrastructure.adapter.rest.mapper.AccountMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
@Tag(name = "Accounts", description = "Account management")
public class AccountController {

    private final CreateAccountInputPort createAccountInputPort;

    public AccountController(CreateAccountInputPort createAccountInputPort) {
        this.createAccountInputPort = createAccountInputPort;
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
}
