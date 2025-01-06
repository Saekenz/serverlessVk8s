#!/bin/bash

# Configuration
GKE_DEPLOYMENT_FILE="order.yaml"
GKE_DEPLOYMENT_NAME="order-deployment"

echo "Starting GKE deployment test..."
start_time=$(date +%s.%3N)
echo "Started timer at $start_time"

# Apply deployment
echo "Applying deployment..."
kubectl apply -f $GKE_DEPLOYMENT_FILE

# Wait for the deployment rollout to complete
echo "Waiting for rollout status..."
kubectl rollout status deployment/$GKE_DEPLOYMENT_NAME

# Check for readiness using curl
until curl -s -o /dev/null -w "%{http_code}" "34.13.168.83.nip.io/orders" | grep -q "200"; do
  echo "Waiting for service to be ready..."
  sleep 2
done

end_time=$(date +%s.%3N)
echo "Ended timer at $end_time"

echo "Deployment and startup completed!"

# Clean up by deleting deployment
kubectl delete -f $GKE_DEPLOYMENT_FILE


