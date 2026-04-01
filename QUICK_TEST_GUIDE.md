# Quick Test Guide - IBM Bob Telco Billing Demo

## 🎨 Interactive Demo Form

**NEW!** We've created an interactive HTML form for easy side-by-side comparison:

### How to Use the Demo Form

1. **Open the form** in your browser:
   - Download `demo-form.html` from the repository
   - Open it locally in any modern browser (Chrome, Firefox, Safari, Edge)
   - Or access it via: `file:///path/to/demo-form.html`

2. **Fill in the invoice details**:
   - Customer ID (e.g., CUST001)
   - Amount (e.g., 99.99)
   - Description (e.g., "Monthly subscription")
   - Due Date (e.g., 2026-05-01)

3. **Click "Create Invoice in Both Systems"**:
   - The form will POST to both legacy and modern systems simultaneously
   - Results appear side-by-side for easy comparison
   - Notice the differences in response format and validation

### Demo Form Features

- ✅ **Side-by-side comparison** of both systems
- ✅ **Real-time API calls** to deployed applications
- ✅ **Visual feedback** with loading states
- ✅ **Error handling** demonstration
- ✅ **Feature comparison table** built-in
- ✅ **No installation required** - just open in browser

### Demo Form URL Endpoints

The form is pre-configured with the correct endpoints:
- **Legacy**: `https://legacy-billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/legacy/invoices`
- **Modern**: `https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/api/v1/invoices`

---

## 🎯 Application URLs

### Modern Billing System (Java 17 + Spring Boot)
**Base URL**: https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com

#### Available Endpoints:

1. **Health Check** (Always works)
   ```
   GET https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/actuator/health
   ```
   Expected: `{"status":"UP"}`

2. **Prometheus Metrics**
   ```
   GET https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/actuator/prometheus
   ```
   Expected: Prometheus metrics in text format

3. **API Documentation (Swagger UI)**
   ```
   https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/swagger-ui.html
   ```
   Expected: Interactive API documentation

4. **OpenAPI Specification**
   ```
   GET https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/v3/api-docs
   ```
   Expected: OpenAPI JSON specification

5. **List All Invoices**
   ```
   GET https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/api/v1/invoices
   ```
   Expected: Empty array `[]` (no invoices yet)

6. **Create Invoice**
   ```bash
   curl -X POST https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/api/v1/invoices \
     -H "Content-Type: application/json" \
     -d '{
       "customerId": "CUST001",
       "amount": 99.99,
       "description": "Monthly subscription",
       "dueDate": "2026-05-01"
     }'
   ```

### Legacy Billing System (Java 8)
**Base URL**: https://legacy-billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com

#### Available Endpoints:

1. **Get Invoice** (Demonstrates SQL Injection vulnerability)
   ```
   GET https://legacy-billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/legacy/invoices/1
   ```
   
   **SQL Injection Test** (DO NOT use in production!):
   ```
   GET https://legacy-billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/legacy/invoices/1%20OR%201=1
   ```

2. **Create Invoice** (No validation)
   ```bash
   curl -X POST https://legacy-billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/legacy/invoices \
     -H "Content-Type: application/json" \
     -d '{
       "customerId": "CUST001",
       "amount": 99.99,
       "description": "Test invoice"
     }'
   ```

**Note**: The legacy app root URL (/) returns a blank page because it only has `/legacy/*` endpoints.

## 🧪 Testing Scenarios

### Scenario 1: Health Check Comparison
```bash
# Modern app - Has health endpoint
curl https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/actuator/health

# Legacy app - No health endpoint (404 error)
curl https://legacy-billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/actuator/health
```

### Scenario 2: API Documentation
```bash
# Modern app - Has Swagger UI
Open: https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/swagger-ui.html

# Legacy app - No documentation
```

### Scenario 3: Metrics & Monitoring
```bash
# Modern app - Prometheus metrics
curl https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/actuator/prometheus

# Legacy app - No metrics
```

### Scenario 4: Security Comparison
```bash
# Modern app - Parameterized queries (safe)
curl "https://billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/api/v1/invoices/1"

# Legacy app - SQL injection vulnerable
curl "https://legacy-billing-app-billing-modernization.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com/legacy/invoices/1%20OR%201=1"
```

## 📊 Expected Results

### Modern App Features ✅
- ✅ Health checks
- ✅ Metrics (Prometheus)
- ✅ API documentation (Swagger)
- ✅ Proper error handling
- ✅ Validation
- ✅ Security (no SQL injection)
- ✅ Structured logging
- ✅ RESTful design

### Legacy App Issues ❌
- ❌ No health checks
- ❌ No metrics
- ❌ No documentation
- ❌ Poor error handling (printStackTrace)
- ❌ No validation
- ❌ SQL injection vulnerabilities
- ❌ System.out.println logging
- ❌ Non-RESTful design

## 🎯 Demo Flow

1. **Start with Health Check**
   - Show modern app health endpoint works
   - Show legacy app has no health endpoint

2. **Show API Documentation**
   - Open Swagger UI for modern app
   - Explain legacy app has no documentation

3. **Test Security**
   - Try SQL injection on legacy app (shows vulnerability)
   - Try same on modern app (safe, parameterized queries)

4. **Show Monitoring**
   - Display Prometheus metrics from modern app
   - Explain legacy app has no metrics

5. **Create Invoices**
   - Create invoice in both systems
   - Compare response quality and error handling

## 🔗 Quick Links

- **GitHub Repo**: https://github.com/barissozen95/bob-case-study
- **Main Guide**: [IBM_Bob_Telco_Efficiency_Guide.md](./IBM_Bob_Telco_Efficiency_Guide.md)
- **Deployment Summary**: [DEPLOYMENT_SUMMARY.md](./DEPLOYMENT_SUMMARY.md)
- **Demo Script**: [openshift/DEMO_SCRIPT.md](./openshift/DEMO_SCRIPT.md)

## 💡 Tips

- Use browser for Swagger UI and health checks
- Use curl or Postman for API testing
- Check OpenShift console for pod logs and metrics
- Both apps connect to the same PostgreSQL database

---

**Note**: The applications are demonstration systems showing "before and after" modernization. The legacy system intentionally contains vulnerabilities for educational purposes only.