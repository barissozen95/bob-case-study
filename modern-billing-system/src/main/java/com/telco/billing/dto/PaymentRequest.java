package com.telco.billing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Payment Request DTO - Java 17 Record
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ Immutable record type (Java 17)
 * ✅ Bean validation
 * ✅ Type-safe
 * ✅ Compact syntax
 */
public record PaymentRequest(
    @NotNull(message = "Invoice ID is required")
    UUID invoiceId,
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    BigDecimal amount,
    
    @NotNull(message = "Payment method is required")
    String paymentMethod
) {
}

// Made with Bob
