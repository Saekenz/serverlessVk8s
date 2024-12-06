#!/bin/bash

# Exit on error
set -e

# Define variables
IMAGE_NAME="alert-service"
GCR_IMAGE="gcr.io/serverless-k8s-study/$IMAGE_NAME"
REGION="europe-west4"
PLATFORM="managed"

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
    --allow-unauthenticated

echo "Deployment completed successfully."
