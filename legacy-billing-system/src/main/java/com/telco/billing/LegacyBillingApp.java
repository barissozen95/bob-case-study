package com.telco.billing;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Legacy Billing Application - Java 8
 * 
 * This is a simplified version that can run in OpenShift
 * to demonstrate the "before" state of modernization
 * 
 * INTENTIONAL ISSUES FOR DEMO:
 * - Simple HTTP server (not production-ready)
 * - Hardcoded database connection
 * - SQL injection vulnerabilities
 * - Poor error handling
 * - No logging framework
 * - No metrics or monitoring
 */
public class LegacyBillingApp {
    
    private static Connection dbConnection;
    
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        
        System.out.println("===========================================");
        System.out.println("  LEGACY BILLING SYSTEM - Java 8");
        System.out.println("  WARNING: This code has security issues!");
        System.out.println("===========================================");
        
        // Initialize database connection (with issues)
        initDatabase();
        
        // Create HTTP server
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        
        // Register endpoints
        server.createContext("/", new HomeHandler());
        server.createContext("/health", new HealthHandler());
        server.createContext("/legacy/invoices", new InvoicesHandler());
        server.createContext("/api/payment", new PaymentHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("Legacy Billing System started on port " + port);
        System.out.println("Access: http://localhost:" + port);
    }
    
    private static void initDatabase() {
        try {
            // ISSUE: Hardcoded credentials
            String dbUrl = System.getenv().getOrDefault("DB_URL", 
                "jdbc:postgresql://postgresql:5432/billing");
            String dbUser = System.getenv().getOrDefault("DB_USER", "billing_user");
            String dbPass = System.getenv().getOrDefault("DB_PASS", "changeme123");
            
            dbConnection = DriverManager.getConnection(dbUrl, dbUser, dbPass);
            System.out.println("Database connected (with hardcoded credentials!)");
            
            // Create table if not exists
            Statement stmt = dbConnection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS legacy_invoices (" +
                "id SERIAL PRIMARY KEY, " +
                "customer_id VARCHAR(50), " +
                "amount DECIMAL(10,2), " +
                "status VARCHAR(20), " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            
            // Insert sample data
            stmt.execute("INSERT INTO legacy_invoices (customer_id, amount, status) " +
                "VALUES ('CUST-001', 150.00, 'PENDING') ON CONFLICT DO NOTHING");
            stmt.execute("INSERT INTO legacy_invoices (customer_id, amount, status) " +
                "VALUES ('CUST-002', 250.00, 'PAID') ON CONFLICT DO NOTHING");
            
            stmt.close();
        } catch (SQLException e) {
            // ISSUE: Poor error handling
            e.printStackTrace();
        }
    }
    
    static class HomeHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers
            addCorsHeaders(exchange);
            String response = "<!DOCTYPE html><html><head><title>Legacy Billing System</title>" +
                "<style>body{font-family:Arial;margin:40px;background:#f5f5f5;}" +
                ".warning{background:#ff6b6b;color:white;padding:20px;border-radius:5px;margin:20px 0;}" +
                ".issue{background:#fff;padding:15px;margin:10px 0;border-left:4px solid #ff6b6b;}" +
                "h1{color:#333;} a{color:#0066cc;text-decoration:none;padding:10px;background:#fff;display:inline-block;margin:5px;border-radius:3px;}" +
                "</style></head><body>" +
                "<h1>⚠️ Legacy Billing System (Java 8)</h1>" +
                "<div class='warning'><strong>WARNING:</strong> This is the LEGACY system with known issues!</div>" +
                "<h2>Known Issues:</h2>" +
                "<div class='issue'>❌ SQL Injection vulnerabilities</div>" +
                "<div class='issue'>❌ Hardcoded database credentials</div>" +
                "<div class='issue'>❌ Poor error handling (printStackTrace)</div>" +
                "<div class='issue'>❌ No logging framework</div>" +
                "<div class='issue'>❌ No metrics or monitoring</div>" +
                "<div class='issue'>❌ Using double for money</div>" +
                "<div class='issue'>❌ No input validation</div>" +
                "<h2>API Endpoints:</h2>" +
                "<a href='/health'>Health Check</a>" +
                "<a href='/api/invoices?customer=CUST-001'>Get Invoices (SQL Injection Risk!)</a>" +
                "<h2>Compare with Modern System:</h2>" +
                "<p>The modern system fixes all these issues with:</p>" +
                "<ul>" +
                "<li>✅ Spring Data JPA (no SQL injection)</li>" +
                "<li>✅ Externalized configuration</li>" +
                "<li>✅ Proper exception handling</li>" +
                "<li>✅ SLF4J logging</li>" +
                "<li>✅ Prometheus metrics</li>" +
                "<li>✅ BigDecimal for money</li>" +
                "<li>✅ Bean Validation</li>" +
                "</ul>" +
                "</body></html>";
            
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    static class HealthHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers
            addCorsHeaders(exchange);
            String response = "{\"status\":\"UP\",\"system\":\"legacy\",\"issues\":\"many\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
    
    static class InvoicesHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers
            addCorsHeaders(exchange);
            
            // Handle OPTIONS request for CORS preflight
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(200, -1);
                return;
            }
            
            // Handle POST request to create invoice
            if ("POST".equals(exchange.getRequestMethod())) {
                handleCreateInvoice(exchange);
                return;
            }
            
            // Handle GET request
            String query = exchange.getRequestURI().getQuery();
            String customerId = "CUST-001"; // default
            
            if (query != null && query.contains("customer=")) {
                customerId = query.split("customer=")[1].split("&")[0];
            }
            
            // ISSUE: SQL Injection vulnerability!
            String sql = "SELECT * FROM legacy_invoices WHERE customer_id = '" + customerId + "'";
            
            System.out.println("VULNERABLE QUERY: " + sql);
            
            StringBuilder response = new StringBuilder();
            response.append("{\"warning\":\"SQL Injection Risk!\",\"query\":\"").append(sql).append("\",\"invoices\":[");
            
            try {
                Statement stmt = dbConnection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                
                boolean first = true;
                while (rs.next()) {
                    if (!first) response.append(",");
                    response.append("{");
                    response.append("\"id\":").append(rs.getInt("id")).append(",");
                    response.append("\"customer_id\":\"").append(rs.getString("customer_id")).append("\",");
                    response.append("\"amount\":").append(rs.getDouble("amount")).append(",");
                    response.append("\"status\":\"").append(rs.getString("status")).append("\"");
                    response.append("}");
                    first = false;
                }
                rs.close();
                stmt.close();
            } catch (SQLException e) {
                // ISSUE: Poor error handling
                e.printStackTrace();
                response.append("],\"error\":\"").append(e.getMessage()).append("\"");
            }
            
            response.append("]}");
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes());
            os.close();
        }
    }
    
    private static void handleCreateInvoice(HttpExchange exchange) throws IOException {
        try {
            // Read request body (Java 8 compatible way)
            StringBuilder requestBody = new StringBuilder();
            java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(exchange.getRequestBody()));
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
            reader.close();
            
            System.out.println("Received invoice creation request: " + requestBody.toString());
            
            // Parse JSON manually (no Jackson in legacy system!)
            String customerId = extractJsonValue(requestBody.toString(), "customerId");
            String amountStr = extractJsonValue(requestBody.toString(), "amount");
            String description = extractJsonValue(requestBody.toString(), "description");
            
            double amount = Double.parseDouble(amountStr);
            
            // Insert into database (with SQL injection risk!)
            String sql = "INSERT INTO legacy_invoices (customer_id, amount, status) VALUES ('" +
                        customerId + "', " + amount + ", 'PENDING') RETURNING id";
            
            System.out.println("VULNERABLE INSERT: " + sql);
            
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            int invoiceId = 0;
            if (rs.next()) {
                invoiceId = rs.getInt(1);
            }
            rs.close();
            stmt.close();
            
            String response = "{\"id\":" + invoiceId +
                            ",\"customerId\":\"" + customerId +
                            "\",\"amount\":" + amount +
                            ",\"description\":\"" + description +
                            "\",\"status\":\"PENDING\"" +
                            ",\"warning\":\"Created with SQL injection vulnerability!\"}";
            
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(201, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
            
        } catch (Exception e) {
            e.printStackTrace();
            String error = "{\"error\":\"" + e.getMessage() + "\"}";
            exchange.sendResponseHeaders(500, error.length());
            OutputStream os = exchange.getResponseBody();
            os.write(error.getBytes());
            os.close();
        }
    }
    
    private static String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int startIndex = json.indexOf(searchKey);
        if (startIndex == -1) return "";
        
        startIndex = json.indexOf(":", startIndex) + 1;
        int endIndex = json.indexOf(",", startIndex);
        if (endIndex == -1) {
            endIndex = json.indexOf("}", startIndex);
        }
        
        String value = json.substring(startIndex, endIndex).trim();
        // Remove quotes if present
        if (value.startsWith("\"")) {
            value = value.substring(1, value.length() - 1);
        }
        return value;
    }
    
    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
        exchange.getResponseHeaders().add("Access-Control-Max-Age", "3600");
    }
    
    static class PaymentHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            // Add CORS headers
            addCorsHeaders(exchange);
            String response = "{\"message\":\"Payment processing not implemented in legacy system\",\"recommendation\":\"Use modern system for payments\"}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(501, response.length());
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

// Made with Bob
