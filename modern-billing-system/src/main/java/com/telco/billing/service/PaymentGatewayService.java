package com.telco.billing.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Payment Gateway Service - Mock implementation
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ Service abstraction
 * ✅ Proper logging
 * ✅ Easy to replace with real implementation
 */
@Service
@Slf4j
public class PaymentGatewayService {
    
    /**
     * Process payment through external gateway
     * In production, this would integrate with actual payment provider
     */
    public boolean processPayment(String customerId, BigDecimal amount, String paymentMethod) {
        log.info("Processing payment: customer={}, amount={}, method={}", 
            customerId, amount, paymentMethod);
        
        // Mock implementation - always succeeds
        // In production, this would call external payment API
        try {
            Thread.sleep(100); // Simulate network call
            log.info("Payment processed successfully");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Payment processing interrupted", e);
            return false;
        }
    }
}

// Made with Bob
