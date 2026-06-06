package com.devsu.fintech.infrastructure.adapter.rest;

import com.devsu.fintech.application.port.input.GenerateAccountReportInputPort;
import com.devsu.fintech.domain.model.AccountReport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "Account statement report")
public class ReportController {

    private final GenerateAccountReportInputPort generateAccountReportInputPort;

    public ReportController(GenerateAccountReportInputPort generateAccountReportInputPort) {
        this.generateAccountReportInputPort = generateAccountReportInputPort;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Generate account statement report for a client in a date range")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    @ApiResponse(responseCode = "404", description = "Client not found")
    public AccountReport generateReport(
            @RequestParam String clientIdentificationNumber,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return generateAccountReportInputPort.execute(clientIdentificationNumber, startDate, endDate);
    }
}
