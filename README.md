# Bachelor's Thesis: Serverless and Kubernetes: A Comparative Study

This repository contains the code for my Bachelor's thesis, titled **"Serverless and Kubernetes: A Comparative Study"**. The thesis compares two cloud computing approaches, **Serverless** and **Kubernetes**, focusing on their deployment processes, performance, scalability,
and cost in the context of a Java-based application that resembles parts of an e-commerce solution. For internal communication, an event-driven approach was selected, utilizing Google’s messaging service **Pub/Sub**.

## Project Overview

For the thesis, a microservices-based e-commerce system using **Java Spring Boot** was implemented. The system is deployed and evaluated on **Google Cloud Run** and **Google Kubernetes Engine (GKE)**, which are used to handle CPU/memory-intensive tasks and scale based on load. The goal is to compare the two environments based on:

- **Deployment**
- **Performance**
- **Scalability**
- **Cost**

The application includes the following services:
- **Catalog Service**: Manages product and product category information.
- **Customer Service**: Manages customer information.
- **Order Service**: Processes orders placed by customers.
- **Inventory Management Service**: Monitors and maintains product stock levels in the database.
- **Inventory Optimization Service**: Uses a variation of a genetic algorithm to redistribute product stock allocation in warehouses based on demand.
- **Alert Service**: Manages system alerts.
