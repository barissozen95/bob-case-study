#!/bin/bash

###############################################################################
# IBM Bob - Deploy Legacy System for Before/After Comparison
###############################################################################

set -e

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║    Deploy Legacy System - Show BEFORE Modernization                ║"
echo "╔════════════════════════════════════════════════════════════════════╗"
echo ""

OCP_API="https://api.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com:6443"
OCP_USER="kubeadmin"
OCP_PASS="9QE4K-YkEJb-DGkun-tPFuP"
NAMESPACE="billing-modernization"

echo "🔐 Logging into OpenShift..."
oc login "$OCP_API" -u "$OCP_USER" -p "$OCP_PASS" --insecure-skip-tls-verify=true

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Building Legacy Application Image"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Create BuildConfig and ImageStream for legacy app
cat <<EOF | oc apply -f -
apiVersion: image.openshift.io/v1
kind: ImageStream
metadata:
  name: legacy-billing-app
  namespace: $NAMESPACE
  labels:
    app: legacy-billing-app
---
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: legacy-billing-app
  namespace: $NAMESPACE
  labels:
    app: legacy-billing-app
spec:
  output:
    to:
      kind: ImageStreamTag
      name: legacy-billing-app:latest
  source:
    type: Git
    git:
      uri: https://github.com/barissozen95/bob-case-study.git
      ref: main
    contextDir: legacy-billing-system
  strategy:
    type: Docker
    dockerStrategy:
      dockerfilePath: ../docker/Dockerfile.legacy
  triggers:
  - type: ConfigChange
EOF

echo ""
echo "🔨 Starting legacy build..."
oc start-build legacy-billing-app -n $NAMESPACE --follow

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Deploying Legacy Application"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

oc apply -f openshift/manifests/04-legacy-app.yaml

echo ""
echo "⏳ Waiting for legacy application to be ready..."
sleep 10
oc wait --for=condition=ready pod -l app=legacy-billing-app -n $NAMESPACE --timeout=300s || {
    echo "⚠️  Legacy app pods not ready yet. Check status with:"
    echo "   oc get pods -n $NAMESPACE"
    echo "   oc logs -l app=legacy-billing-app -n $NAMESPACE"
}

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Legacy System Deployed!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

LEGACY_URL=$(oc get route legacy-billing-app -n $NAMESPACE -o jsonpath='{.spec.host}' 2>/dev/null || echo "Route not found")
MODERN_URL=$(oc get route billing-app -n $NAMESPACE -o jsonpath='{.spec.host}' 2>/dev/null || echo "Route not found")

echo "🌐 Application URLs:"
echo ""
echo "   LEGACY (Java 8):  https://$LEGACY_URL"
echo "   MODERN (Java 17): https://$MODERN_URL"
echo ""
echo "📊 Compare the systems:"
echo ""
echo "   Legacy Home:      https://$LEGACY_URL/"
echo "   Legacy Health:    https://$LEGACY_URL/health"
echo "   Legacy API:       https://$LEGACY_URL/api/invoices?customer=CUST-001"
echo ""
echo "   Modern Swagger:   https://$MODERN_URL/swagger-ui.html"
echo "   Modern Health:    https://$MODERN_URL/actuator/health"
echo "   Modern Metrics:   https://$MODERN_URL/actuator/prometheus"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 Demo Flow:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. Show Legacy System Issues:"
echo "   open https://$LEGACY_URL/"
echo "   - Point out SQL injection warning"
echo "   - Show hardcoded credentials"
echo "   - No metrics or monitoring"
echo ""
echo "2. Show Modern System Benefits:"
echo "   open https://$MODERN_URL/swagger-ui.html"
echo "   - Spring Data JPA (no SQL injection)"
echo "   - Externalized configuration"
echo "   - Comprehensive metrics"
echo ""
echo "3. Compare Side-by-Side:"
echo "   oc get pods -n $NAMESPACE"
echo "   oc logs -l app=legacy-billing-app -n $NAMESPACE"
echo "   oc logs -l app=billing-app -n $NAMESPACE"
echo ""

# Made with Bob
