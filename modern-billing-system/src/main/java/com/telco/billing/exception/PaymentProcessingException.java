package com.telco.billing.exception;

/**
 * Exception thrown when payment processing fails
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ Custom exception for payment errors
 * ✅ Clear error messaging
 * ✅ Proper exception hierarchy
 */
public class PaymentProcessingException extends RuntimeException {
    
    public PaymentProcessingException(String message) {
        super(message);
    }
    
    public PaymentProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Made with Bob
