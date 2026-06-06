package com.devsu.fintech.infrastructure.adapter.rest;

import com.devsu.fintech.application.port.input.RegisterTransactionInputPort;
import com.devsu.fintech.domain.model.Transaction;
import com.devsu.fintech.infrastructure.adapter.rest.dto.RegisterTransactionRequestDTO;
import com.devsu.fintech.infrastructure.adapter.rest.dto.RegisterTransactionResponseDTO;
import com.devsu.fintech.infrastructure.adapter.rest.mapper.TransactionMapper;
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
@RequestMapping("/transactions")
@Tag(name = "Transactions", description = "Transaction management")
public class TransactionController {

    private final RegisterTransactionInputPort registerTransactionInputPort;

    public TransactionController(RegisterTransactionInputPort registerTransactionInputPort) {
        this.registerTransactionInputPort = registerTransactionInputPort;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new transaction and update account balance")
    @ApiResponse(responseCode = "201", description = "Transaction registered successfully")
    @ApiResponse(responseCode = "404", description = "Account not found")
    @ApiResponse(responseCode = "422", description = "Account closed or insufficient funds")
    public RegisterTransactionResponseDTO registerTransaction(
            @RequestBody RegisterTransactionRequestDTO request) {
        Transaction saved = registerTransactionInputPort.execute(TransactionMapper.toTransaction(request));
        return TransactionMapper.toResponseDTO(saved);
    }

}
