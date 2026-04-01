package com.telco.billing.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Modern Invoice Entity - Java 17
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ JPA entity with proper annotations
 * ✅ Bean validation
 * ✅ BigDecimal for monetary values
 * ✅ LocalDateTime instead of Date
 * ✅ Enum for status
 * ✅ Lombok for boilerplate reduction
 * ✅ UUID for primary key
 * ✅ Audit fields
 * ✅ Optimistic locking
 */
@Entity
@Table(name = "invoices", indexes = {
    @Index(name = "idx_customer_id", columnList = "customer_id"),
    @Index(name = "idx_invoice_date", columnList = "invoice_date"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @NotNull(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false, length = 50)
    private String customerId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "paid_amount", precision = 10, scale = 2)
    private BigDecimal paidAmount;
    
    @Column(name = "invoice_date", nullable = false)
    private LocalDateTime invoiceDate;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public enum InvoiceStatus {
        PENDING,
        PAID,
        OVERDUE,
        CANCELLED,
        REFUNDED
    }
}

// Made with Bob
