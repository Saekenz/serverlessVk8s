#!/bin/bash

# List of Kubernetes manifest files
YAML_FILES=(
  "alert.yaml"
  "catalog.yaml"
  "customer.yaml"
  "inventory-mgmt.yaml"
  "inventory-opt.yaml"
  "order.yaml"
  "db-config-secret.yaml"
)

# Loop through the listed files and delete them
for file in "${YAML_FILES[@]}"; do
  echo "Deleting $file..."

  if kubectl delete -f "$file"; then
    echo "$file deployment deleted successfully."
  else
    echo "Failed to delete $file." >&2
    exit 1
  fi
done

echo "All deployments deleted successfully."