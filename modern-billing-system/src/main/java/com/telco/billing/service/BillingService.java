package com.telco.billing.service;

import com.telco.billing.domain.Invoice;
import com.telco.billing.domain.Invoice.InvoiceStatus;
import com.telco.billing.dto.PaymentRequest;
import com.telco.billing.dto.PaymentResponse;
import com.telco.billing.exception.InvoiceNotFoundException;
import com.telco.billing.exception.PaymentProcessingException;
import com.telco.billing.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Modern Billing Service - Java 17 with Spring Boot
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ Dependency injection via constructor
 * ✅ Proper transaction management
 * ✅ Comprehensive logging with SLF4J
 * ✅ Metrics and monitoring with Micrometer
 * ✅ Proper exception handling
 * ✅ Type-safe operations
 * ✅ Business logic separation
 * ✅ Immutable DTOs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {
    
    private final InvoiceRepository invoiceRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final NotificationService notificationService;
    private final MeterRegistry meterRegistry;
    
    /**
     * Retrieves all invoices for a specific customer
     * 
     * @param customerId the customer identifier
     * @return list of invoices
     */
    @Transactional(readOnly = true)
    public List<Invoice> getCustomerInvoices(String customerId) {
        log.info("Fetching invoices for customer: {}", customerId);
        
        try {
            List<Invoice> invoices = invoiceRepository.findByCustomerId(customerId);
            log.info("Found {} invoices for customer: {}", invoices.size(), customerId);
            
            // Record metric
            meterRegistry.counter("billing.invoices.retrieved", 
                "customer_id", customerId).increment(invoices.size());
            
            return invoices;
        } catch (Exception e) {
            log.error("Error fetching invoices for customer: {}", customerId, e);
            incrementCounter("billing.invoices.error");
            throw new RuntimeException("Failed to retrieve invoices", e);
        }
    }
    
    /**
     * Creates a new invoice
     *
     * @param invoice the invoice to create
     * @return created invoice with generated ID
     */
    @Transactional
    public Invoice createInvoice(Invoice invoice) {
        log.info("Creating new invoice for customer: {}", invoice.getCustomerId());
        
        try {
            // Set initial values
            invoice.setStatus(InvoiceStatus.PENDING);
            invoice.setCreatedAt(LocalDateTime.now());
            invoice.setUpdatedAt(LocalDateTime.now());
            
            // Save invoice
            Invoice saved = invoiceRepository.save(invoice);
            
            // Record metric
            incrementCounter("billing.invoices.created");
            
            log.info("Invoice created successfully with ID: {}", saved.getId());
            return saved;
        } catch (Exception e) {
            log.error("Error creating invoice", e);
            incrementCounter("billing.invoices.create.error");
            throw new RuntimeException("Failed to create invoice", e);
        }
    }
    
    /**
     * Retrieves all invoices
     *
     * @return list of all invoices
     */
    @Transactional(readOnly = true)
    public List<Invoice> getAllInvoices() {
        log.info("Fetching all invoices");
        
        try {
            List<Invoice> invoices = invoiceRepository.findAll();
            log.info("Found {} invoices", invoices.size());
            
            // Record metric
            incrementCounter("billing.invoices.list");
            
            return invoices;
        } catch (Exception e) {
            log.error("Error fetching all invoices", e);
            incrementCounter("billing.invoices.error");
            throw new RuntimeException("Failed to retrieve invoices", e);
        }
    }
    
    /**
     * Processes a payment for an invoice with full transaction support
     * 
     * @param request payment request details
     * @return payment response with status
     * @throws InvoiceNotFoundException if invoice doesn't exist
     * @throws PaymentProcessingException if payment fails
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        UUID invoiceId = request.invoiceId();
        BigDecimal amount = request.amount();
        
        log.info("Processing payment for invoice: {}, amount: {}", invoiceId, amount);
        
        // Start timer for metrics
        Timer.Sample timer = Timer.start(meterRegistry);
        
        try {
            // 1. Retrieve and validate invoice
            Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new InvoiceNotFoundException(
                    "Invoice not found: " + invoiceId));
            
            validatePayment(invoice, amount);
            
            // 2. Process payment through gateway
            boolean paymentSuccess = paymentGatewayService.processPayment(
                invoice.getCustomerId(),
                amount,
                request.paymentMethod()
            );
            
            if (!paymentSuccess) {
                incrementCounter("billing.payment.failed");
                throw new PaymentProcessingException("Payment gateway declined");
            }
            
            // 3. Update invoice status
            invoice.setPaidAmount(amount);
            invoice.setStatus(InvoiceStatus.PAID);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            
            // 4. Send notification
            notificationService.sendPaymentConfirmation(
                invoice.getCustomerId(),
                invoice,
                amount
            );
            
            // 5. Record success metrics
            incrementCounter("billing.payment.success");
            timer.stop(meterRegistry.timer("billing.payment.processing.time"));
            
            log.info("Payment processed successfully for invoice: {}", invoiceId);
            
            return new PaymentResponse(
                true,
                invoiceId,
                amount,
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                "Payment processed successfully"
            );
            
        } catch (InvoiceNotFoundException | PaymentProcessingException e) {
            log.error("Payment processing failed for invoice: {}", invoiceId, e);
            incrementCounter("billing.payment.error");
            timer.stop(meterRegistry.timer("billing.payment.processing.time", "status", "error"));
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing payment for invoice: {}", invoiceId, e);
            incrementCounter("billing.payment.error");
            timer.stop(meterRegistry.timer("billing.payment.processing.time", "status", "error"));
            throw new PaymentProcessingException("Payment processing failed", e);
        }
    }
    
    /**
     * Validates payment against invoice
     */
    private void validatePayment(Invoice invoice, BigDecimal amount) {
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new PaymentProcessingException("Invoice already paid");
        }
        
        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new PaymentProcessingException("Cannot pay cancelled invoice");
        }
        
        if (amount.compareTo(invoice.getAmount()) != 0) {
            throw new PaymentProcessingException(
                String.format("Payment amount %s does not match invoice amount %s",
                    amount, invoice.getAmount())
            );
        }
    }
    
    /**
     * Calculates total revenue for a date range
     */
    @Transactional(readOnly = true)
    public BigDecimal calculateRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating revenue from {} to {}", startDate, endDate);
        
        return invoiceRepository.calculateRevenue(startDate, endDate)
            .orElse(BigDecimal.ZERO);
    }
    
    /**
     * Identifies and marks overdue invoices
     */
    @Transactional
    public void processOverdueInvoices() {
        log.info("Processing overdue invoices");
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(cutoffDate);
        
        log.info("Found {} overdue invoices", overdueInvoices.size());
        
        overdueInvoices.forEach(invoice -> {
            invoice.setStatus(InvoiceStatus.OVERDUE);
            invoice.setUpdatedAt(LocalDateTime.now());
            invoiceRepository.save(invoice);
            
            // Send overdue notification
            notificationService.sendOverdueNotification(
                invoice.getCustomerId(),
                invoice
            );
        });
        
        incrementCounter("billing.invoices.overdue", overdueInvoices.size());
    }
    
    /**
     * Helper method to increment metrics counter
     */
    private void incrementCounter(String counterName) {
        incrementCounter(counterName, 1);
    }
    
    private void incrementCounter(String counterName, long amount) {
        meterRegistry.counter(counterName).increment(amount);
    }
}

// Made with Bob
