#!/bin/bash

# Exit on error
set -e

# Ensure all required arguments are supplied
if [ "$#" -ne 3 ]; then
  echo "Usage: $0 <DATABASE_HOST> <DATABASE_USER> <DATABASE_PASSWORD>"
  exit 1
fi

# Define variables
IMAGE_NAME="alert-service"
GCR_IMAGE="gcr.io/serverless-k8s-study/$IMAGE_NAME"
REGION="europe-west4"
PLATFORM="managed"
DATABASE_HOST=$1
DATABASE_USER=$2
DATABASE_PASSWORD=$3

# Build and tag the Docker image
echo "Building Docker image..."
docker build -t $GCR_IMAGE .

# Push the image to GCR
echo "Pushing Docker image to Google Container Registry..."
docker push $GCR_IMAGE

# Deploy the service to Google Cloud Run
echo "Deploying $IMAGE_NAME to Google Cloud Run..."
gcloud run deploy $IMAGE_NAME \
    --image $GCR_IMAGE \
    --platform $PLATFORM \
    --region $REGION \
    --allow-unauthenticated \
    --set-env-vars DATABASE_HOST="$DATABASE_HOST",DATABASE_USER="$DATABASE_USER",DATABASE_PASSWORD="$DATABASE_PASSWORD"
echo "Deployment completed successfully."
