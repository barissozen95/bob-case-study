package com.telco.billing.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Payment Response DTO - Java 17 Record
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ Immutable record type
 * ✅ Type-safe
 * ✅ Clear API contract
 */
public record PaymentResponse(
    boolean success,
    UUID invoiceId,
    BigDecimal amount,
    String transactionId,
    LocalDateTime timestamp,
    String message
) {
}

// Made with Bob
