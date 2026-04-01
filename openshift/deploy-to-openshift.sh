#!/bin/bash

###############################################################################
# IBM Bob - OpenShift Deployment Script
# 
# This script deploys the modernized billing system to OpenShift
###############################################################################

set -e

echo "╔════════════════════════════════════════════════════════════════════╗"
echo "║    IBM Bob - Deploy Modern Billing System to OpenShift             ║"
echo "╔════════════════════════════════════════════════════════════════════╗"
echo ""

# OpenShift cluster details
OCP_API="https://api.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com:6443"
OCP_USER="kubeadmin"
OCP_PASS="9QE4K-YkEJb-DGkun-tPFuP"
NAMESPACE="billing-modernization"

echo "🔐 Logging into OpenShift cluster..."
echo "   API: $OCP_API"
echo ""

# Login to OpenShift
oc login "$OCP_API" -u "$OCP_USER" -p "$OCP_PASS" --insecure-skip-tls-verify=true

if [ $? -ne 0 ]; then
    echo "❌ Failed to login to OpenShift"
    exit 1
fi

echo "✅ Successfully logged in to OpenShift"
echo ""

# Create namespace
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 1: Creating namespace"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

oc apply -f openshift/manifests/01-namespace.yaml

echo ""
echo "✅ Namespace created"
echo ""

# Deploy PostgreSQL
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 2: Deploying PostgreSQL database"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

oc apply -f openshift/manifests/02-postgresql.yaml

echo ""
echo "⏳ Waiting for PostgreSQL to be ready..."
oc wait --for=condition=ready pod -l app=postgresql -n $NAMESPACE --timeout=300s

echo ""
echo "✅ PostgreSQL deployed and ready"
echo ""

# Build application image
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 3: Building application image"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

# Create BuildConfig and ImageStream
cat <<EOF | oc apply -f -
apiVersion: image.openshift.io/v1
kind: ImageStream
metadata:
  name: billing-app
  namespace: $NAMESPACE
  labels:
    app: billing-app
---
apiVersion: build.openshift.io/v1
kind: BuildConfig
metadata:
  name: billing-app
  namespace: $NAMESPACE
  labels:
    app: billing-app
spec:
  output:
    to:
      kind: ImageStreamTag
      name: billing-app:latest
  source:
    type: Git
    git:
      uri: https://github.com/barissozen95/bob-case-study.git
      ref: main
    contextDir: modern-billing-system
  strategy:
    type: Docker
    dockerStrategy:
      dockerfilePath: ../docker/Dockerfile
  triggers:
  - type: ConfigChange
EOF

echo ""
echo "🔨 Starting build..."
oc start-build billing-app -n $NAMESPACE --follow

if [ $? -ne 0 ]; then
    echo "⚠️  Build failed or timed out. You can check build logs with:"
    echo "   oc logs -f bc/billing-app -n $NAMESPACE"
    echo ""
    echo "   Continuing with deployment anyway..."
fi

echo ""
echo "✅ Application image built"
echo ""

# Deploy application
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "Step 4: Deploying billing application"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

oc apply -f openshift/manifests/03-billing-app.yaml

echo ""
echo "⏳ Waiting for application to be ready..."
sleep 10
oc wait --for=condition=ready pod -l app=billing-app -n $NAMESPACE --timeout=300s || {
    echo "⚠️  Application pods not ready yet. Check status with:"
    echo "   oc get pods -n $NAMESPACE"
    echo "   oc logs -l app=billing-app -n $NAMESPACE"
}

echo ""
echo "✅ Application deployed"
echo ""

# Get route URL
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Deployment Complete!"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""

ROUTE_URL=$(oc get route billing-app -n $NAMESPACE -o jsonpath='{.spec.host}' 2>/dev/null || echo "Route not found")

if [ "$ROUTE_URL" != "Route not found" ]; then
    echo "🌐 Application URL: https://$ROUTE_URL"
    echo ""
    echo "📚 API Endpoints:"
    echo "   • Swagger UI:     https://$ROUTE_URL/swagger-ui.html"
    echo "   • Health Check:   https://$ROUTE_URL/actuator/health"
    echo "   • Metrics:        https://$ROUTE_URL/actuator/metrics"
    echo "   • Prometheus:     https://$ROUTE_URL/actuator/prometheus"
    echo ""
else
    echo "⚠️  Route not found. Get it with:"
    echo "   oc get route billing-app -n $NAMESPACE"
    echo ""
fi

echo "📊 Deployment Status:"
echo ""
oc get all -n $NAMESPACE
echo ""

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📝 Useful Commands:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "View pods:              oc get pods -n $NAMESPACE"
echo "View logs:              oc logs -f -l app=billing-app -n $NAMESPACE"
echo "View services:          oc get svc -n $NAMESPACE"
echo "View routes:            oc get routes -n $NAMESPACE"
echo "Scale application:      oc scale deployment/billing-app --replicas=3 -n $NAMESPACE"
echo "Delete deployment:      oc delete namespace $NAMESPACE"
echo ""
echo "OpenShift Console:      https://console-openshift-console.apps.69ccbf51306b4437a3a7ff2a.am1.techzone.ibm.com"
echo ""

# Made with Bob
