#!/bin/bash

# List of service endpoints
ENDPOINTS=(
  "34.91.124.111.nip.io/orders"
  "34.91.124.111.nip.io/catalog/products"
  "34.91.124.111.nip.io/customers"
  "34.91.124.111.nip.io/optimizations/config")

# List of Kubernetes manifest files
YAML_FILES=(
  "db-config-secret.yaml"
  "alert.yaml"
  "catalog.yaml"
  "customer.yaml"
  "inventory-mgmt.yaml"
  "inventory-opt.yaml"
  "order.yaml"
)

echo "Starting GKE deployment test..."
start_time=$(date +%s.%3N)
echo "Started timer at $start_time"

# Loop through the listed files and apply them
for file in "${YAML_FILES[@]}"; do
  echo "Applying $file..."

  if kubectl apply -f "$file"; then
    echo "$file applied successfully."
  else
    echo "Failed to apply $file." >&2
    exit 1
  fi
done

# Check for readiness using curl
echo "Checking readiness for all endpoints..."
check_endpoint() {
  endpoint=$1
  until curl -s -o /dev/null -w "%{http_code}" "$endpoint" | grep -q "200"; do
    echo "$endpoint is not ready yet, retrying in 2 seconds..."
    sleep 2
  done
  echo "$endpoint is ready!"
}

# Run all endpoint checks in parallel
for endpoint in "${ENDPOINTS[@]}"; do
  check_endpoint $endpoint &
done

# Wait for all background processes to complete
wait

end_time=$(date +%s.%3N)
echo "Ended timer at $end_time"

echo "All endpoints are ready. Deployment and startup completed!"
echo "$start_time,$end_time" >> gke_deployment_times.csv

