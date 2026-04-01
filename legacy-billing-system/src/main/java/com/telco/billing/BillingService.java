package com.telco.billing;

import java.sql.*;
import java.util.*;

/**
 * Legacy Billing Service - Java 8
 * 
 * ISSUES IDENTIFIED BY IBM BOB:
 * - SQL Injection vulnerability
 * - Hardcoded credentials
 * - No connection pooling
 * - Poor error handling
 * - No transaction management
 * - Tight coupling to database
 * - No logging or monitoring
 */
public class BillingService {
    private Connection dbConnection;
    
    public BillingService() {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            dbConnection = DriverManager.getConnection(
                "jdbc:oracle:thin:@localhost:1521:xe", 
                "admin", 
                "password123"  // SECURITY ISSUE: Hardcoded credentials
            );
        } catch (Exception e) {
            e.printStackTrace();  // ISSUE: Poor error handling
        }
    }
    
    /**
     * Get customer invoices
     * WARNING: SQL Injection vulnerability!
     */
    public List<Invoice> getCustomerInvoices(String customerId) {
        List<Invoice> invoices = new ArrayList<>();
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = dbConnection.createStatement();
            // SECURITY ISSUE: SQL Injection vulnerability
            String query = "SELECT * FROM INVOICES WHERE CUSTOMER_ID = '" + customerId + "'";
            rs = stmt.executeQuery(query);
            
            while (rs.next()) {
                Invoice invoice = new Invoice();
                invoice.setId(rs.getString("ID"));
                invoice.setCustomerId(rs.getString("CUSTOMER_ID"));
                invoice.setAmount(rs.getDouble("AMOUNT"));  // ISSUE: Using double for money
                invoice.setDate(rs.getDate("INVOICE_DATE"));
                invoices.add(invoice);
            }
        } catch (SQLException e) {
            e.printStackTrace();  // ISSUE: Poor error handling
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return invoices;
    }
    
    /**
     * Process payment
     * WARNING: No transaction management!
     */
    public boolean processPayment(String invoiceId, double amount) {
        Statement stmt = null;
        
        try {
            stmt = dbConnection.createStatement();
            // SECURITY ISSUE: SQL Injection vulnerability
            String update = "UPDATE INVOICES SET STATUS = 'PAID', PAID_AMOUNT = " 
                + amount + " WHERE ID = '" + invoiceId + "'";
            int rows = stmt.executeUpdate(update);
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();  // ISSUE: No proper error handling
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Calculate total revenue
     * ISSUE: No error handling, inefficient query
     */
    public double calculateTotalRevenue(String startDate, String endDate) {
        double total = 0.0;
        Statement stmt = null;
        ResultSet rs = null;
        
        try {
            stmt = dbConnection.createStatement();
            // SECURITY ISSUE: SQL Injection vulnerability
            String query = "SELECT SUM(AMOUNT) as TOTAL FROM INVOICES " +
                          "WHERE INVOICE_DATE BETWEEN '" + startDate + "' AND '" + endDate + "'";
            rs = stmt.executeQuery(query);
            
            if (rs.next()) {
                total = rs.getDouble("TOTAL");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
        return total;
    }
    
    /**
     * Close database connection
     * ISSUE: Manual resource management
     */
    public void close() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

// Made with Bob
