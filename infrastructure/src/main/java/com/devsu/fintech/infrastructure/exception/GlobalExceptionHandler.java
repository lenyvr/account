package com.devsu.fintech.infrastructure.exception;

import com.devsu.fintech.domain.exception.AccountClosedException;
import com.devsu.fintech.domain.exception.AccountClosureNotAllowedViaUpdateException;
import com.devsu.fintech.domain.exception.AccountNotFoundException;
import com.devsu.fintech.domain.exception.ClientNotFoundException;
import com.devsu.fintech.domain.exception.InvalidDormantTransitionException;
import com.devsu.fintech.domain.exception.InsufficientFundsException;
import com.devsu.fintech.domain.exception.InvalidRefundMethodException;
import com.devsu.fintech.domain.exception.TargetAccountRequiredException;
import com.devsu.fintech.infrastructure.adapter.rest.dto.ErrorResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AccountNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleAccountNotFound(AccountNotFoundException ex) {
        log.error("Account not found: {}", ex.getMessage());
        return new ErrorResponseDTO(ex.getMessage());
    }

    @ExceptionHandler({
        AccountClosedException.class,
        AccountClosureNotAllowedViaUpdateException.class,
        InvalidDormantTransitionException.class,
        InvalidRefundMethodException.class,
        TargetAccountRequiredException.class,
        InsufficientFundsException.class,
        IllegalArgumentException.class
    })
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponseDTO handleBusinessRuleViolation(RuntimeException ex) {
        log.error("Business rule violation: {}", ex.getMessage());
        return new ErrorResponseDTO(ex.getMessage());
    }

    @ExceptionHandler(ClientNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponseDTO handleClientNotFound(ClientNotFoundException ex) {
        log.error("Client not found: {}", ex.getMessage());
        return new ErrorResponseDTO(ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponseDTO handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.error("Data integrity violation: {}", ex.getMessage());
        return new ErrorResponseDTO("Account number already exists");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponseDTO handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return new ErrorResponseDTO("An unexpected error occurred");
    }
}
