#!/bin/bash

echo "Starting GKE deployment test..."
start_time=$(date +%s.%3N)
echo "Started timer at $start_time"

# Submit deployment
echo "Applying deployment..."
gcloud builds submit --config cloudbuild.yaml

# Check for readiness using curl
until curl -s -o /dev/null -w "%{http_code}" "https://ecommerce-api-gateway-b7sy7t08.ew.gateway.dev/orders" | grep -q "200"; do
  echo "Waiting for service to be ready..."
  sleep 2
done

end_time=$(date +%s.%3N)
echo "Ended timer at $end_time"

echo "Deployment and startup completed!"
echo "$start_time,$end_time" >> cloud_run_deployment_times.csv