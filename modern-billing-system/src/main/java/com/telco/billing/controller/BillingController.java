package com.telco.billing.controller;

import com.telco.billing.domain.Invoice;
import com.telco.billing.dto.PaymentRequest;
import com.telco.billing.dto.PaymentResponse;
import com.telco.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Modern REST Controller - Spring Boot 3.x
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ RESTful API design
 * ✅ OpenAPI/Swagger documentation
 * ✅ Proper HTTP status codes
 * ✅ Request validation
 * ✅ Comprehensive logging
 * ✅ Exception handling
 */
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Billing", description = "Billing and invoice management APIs")
public class BillingController {
    
    private final BillingService billingService;
    
    @Operation(summary = "Get customer invoices", 
               description = "Retrieves all invoices for a specific customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved invoices"),
        @ApiResponse(responseCode = "400", description = "Invalid customer ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/customers/{customerId}/invoices")
    public ResponseEntity<List<Invoice>> getCustomerInvoices(
            @Parameter(description = "Customer ID", required = true)
            @PathVariable String customerId) {
        
        log.info("REST request to get invoices for customer: {}", customerId);
        List<Invoice> invoices = billingService.getCustomerInvoices(customerId);
        return ResponseEntity.ok(invoices);
    }
    
    @Operation(summary = "Process payment", 
               description = "Processes a payment for an invoice")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment request"),
        @ApiResponse(responseCode = "404", description = "Invoice not found"),
        @ApiResponse(responseCode = "500", description = "Payment processing failed")
    })
    @PostMapping("/payments")
    public ResponseEntity<PaymentResponse> processPayment(
            @Valid @RequestBody PaymentRequest request) {
        
        log.info("REST request to process payment: {}", request);
        PaymentResponse response = billingService.processPayment(request);
        return ResponseEntity.ok(response);
    }
    
    @Operation(summary = "Calculate revenue", 
               description = "Calculates total revenue for a date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Revenue calculated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/revenue")
    public ResponseEntity<BigDecimal> calculateRevenue(
            @Parameter(description = "Start date (ISO format)")
            @RequestParam LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)")
            @RequestParam LocalDateTime endDate) {
        
        log.info("REST request to calculate revenue from {} to {}", startDate, endDate);
        BigDecimal revenue = billingService.calculateRevenue(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }
    
    @Operation(summary = "Process overdue invoices", 
               description = "Batch process to identify and mark overdue invoices")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "202", description = "Processing started"),
        @ApiResponse(responseCode = "500", description = "Processing failed")
    })
    @PostMapping("/invoices/process-overdue")
    public ResponseEntity<Void> processOverdueInvoices() {
        log.info("REST request to process overdue invoices");
        billingService.processOverdueInvoices();
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}

// Made with Bob
