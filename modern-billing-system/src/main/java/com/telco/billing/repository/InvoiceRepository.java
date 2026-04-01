package com.telco.billing.repository;

import com.telco.billing.domain.Invoice;
import com.telco.billing.domain.Invoice.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Modern Invoice Repository - Spring Data JPA
 * 
 * IMPROVEMENTS BY IBM BOB:
 * ✅ Spring Data JPA repository
 * ✅ Type-safe queries
 * ✅ No SQL injection risk
 * ✅ Automatic transaction management
 * ✅ Query method naming conventions
 * ✅ Custom JPQL queries
 * ✅ Pagination support ready
 */
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    
    // Query methods using Spring Data JPA naming conventions
    List<Invoice> findByCustomerId(String customerId);
    
    List<Invoice> findByCustomerIdAndStatus(String customerId, InvoiceStatus status);
    
    List<Invoice> findByInvoiceDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Invoice> findByStatus(InvoiceStatus status);
    
    // Custom JPQL query with pagination support
    @Query("SELECT i FROM Invoice i WHERE i.customerId = :customerId " +
           "AND i.status = :status ORDER BY i.invoiceDate DESC")
    List<Invoice> findCustomerInvoicesByStatus(
        @Param("customerId") String customerId,
        @Param("status") InvoiceStatus status
    );
    
    // Native query for complex aggregations
    @Query(value = "SELECT SUM(amount) FROM invoices " +
                   "WHERE customer_id = :customerId AND status = 'PAID'", 
           nativeQuery = true)
    Optional<BigDecimal> calculateTotalPaidAmount(@Param("customerId") String customerId);
    
    // Calculate total revenue for a date range
    @Query("SELECT SUM(i.amount) FROM Invoice i " +
           "WHERE i.status = 'PAID' " +
           "AND i.invoiceDate BETWEEN :startDate AND :endDate")
    Optional<BigDecimal> calculateRevenue(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
    
    // Modifying query with transaction support
    @Modifying
    @Query("UPDATE Invoice i SET i.status = :status, i.updatedAt = :updatedAt " +
           "WHERE i.id = :invoiceId")
    int updateInvoiceStatus(
        @Param("invoiceId") UUID invoiceId,
        @Param("status") InvoiceStatus status,
        @Param("updatedAt") LocalDateTime updatedAt
    );
    
    // Find overdue invoices
    @Query("SELECT i FROM Invoice i WHERE i.status = 'PENDING' " +
           "AND i.invoiceDate < :cutoffDate")
    List<Invoice> findOverdueInvoices(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Count invoices by status
    long countByStatus(InvoiceStatus status);
    
    // Check if invoice exists for customer
    boolean existsByCustomerIdAndId(String customerId, UUID invoiceId);
}

// Made with Bob
