#!/bin/bash

# List of Kubernetes manifest files
YAML_FILES=(
  "alert.yaml"
  "catalog.yaml"
  "customer.yaml"
  "inventory-mgmt.yaml"
  "inventory-opt.yaml"
  "order.yaml"
)

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

echo "All deployments applied successfully."