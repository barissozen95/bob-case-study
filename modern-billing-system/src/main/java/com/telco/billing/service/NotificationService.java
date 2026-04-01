package com.telco.billing.service;

import com.telco.billing.domain.Invoice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Notification Service - Mock implementation
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ Service abstraction
 * ✅ Proper logging
 * ✅ Easy to extend with email/SMS providers
 */
@Service
@Slf4j
public class NotificationService {
    
    /**
     * Send payment confirmation notification
     */
    public void sendPaymentConfirmation(String customerId, Invoice invoice, BigDecimal amount) {
        log.info("Sending payment confirmation: customer={}, invoice={}, amount={}", 
            customerId, invoice.getId(), amount);
        
        // Mock implementation
        // In production, this would send email/SMS
        log.info("Payment confirmation sent successfully");
    }
    
    /**
     * Send overdue invoice notification
     */
    public void sendOverdueNotification(String customerId, Invoice invoice) {
        log.info("Sending overdue notification: customer={}, invoice={}", 
            customerId, invoice.getId());
        
        // Mock implementation
        // In production, this would send email/SMS
        log.info("Overdue notification sent successfully");
    }
}

// Made with Bob
