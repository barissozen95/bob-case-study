package com.telco.billing.exception;

/**
 * Exception thrown when an invoice is not found
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ Custom exception for specific error handling
 * ✅ Clear error messaging
 * ✅ Proper exception hierarchy
 */
public class InvoiceNotFoundException extends RuntimeException {
    
    public InvoiceNotFoundException(String message) {
        super(message);
    }
    
    public InvoiceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Made with Bob
