package com.telco.billing;

import java.util.Date;

/**
 * Legacy Invoice class - Java 8
 * 
 * ISSUES IDENTIFIED BY IBM BOB:
 * - No validation
 * - Mutable fields
 * - Using Date instead of LocalDateTime
 * - No builder pattern
 * - No proper equals/hashCode
 */
public class Invoice {
    private String id;
    private String customerId;
    private double amount;  // ISSUE: Using double for money
    private Date date;      // ISSUE: Using old Date API
    private String status;
    
    // Default constructor
    public Invoice() {
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;  // ISSUE: No validation
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;  // ISSUE: No validation
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;  // ISSUE: No validation, using double for money
    }
    
    public Date getDate() {
        return date;
    }
    
    public void setDate(Date date) {
        this.date = date;  // ISSUE: Mutable Date object
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;  // ISSUE: No enum, no validation
    }
    
    @Override
    public String toString() {
        return "Invoice{" +
                "id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", amount=" + amount +
                ", date=" + date +
                ", status='" + status + '\'' +
                '}';
    }
}

// Made with Bob
