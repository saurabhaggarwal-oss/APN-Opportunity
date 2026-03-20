# Apn Opportunity Project

## Overview
The **Apn Opportunity** project is a robust Spring Boot microservice designed to manage and process Amazon Partner Network (APN) opportunities. It provides a centralized platform for listing, filtering, and managing opportunity data, with advanced features like AI-powered workload generation and automated data refresh.

## Key Features
- **Opportunity Management**: List, filter, and track APN opportunities with ease.
- **AI-Driven Workload Generation**: Integrates with **OpenAI (GPT-4o)** via Spring AI for intelligent processing and workload generation.
- **Automated Data Refresh**: Uses **RabbitMQ** for asynchronous and parallel processing of data refresh tasks.
- **Snowflake Integration**: High-performance data storage and retrieval using **Snowflake**.
- **Excel Export**: Export master and raw data to Excel for offline analysis (powered by Apache POI).
- **Centralized Error Handling**: Unified error management across modules.
- **API Documentation**: Interactive API documentation via **SpringDoc OpenAPI (Swagger)**.
- **Monitoring & Metrics**: Health checks and performance metrics via **Spring Boot Actuator** and **Prometheus**.

## Technology Stack
- **Languages**: Java 21
- **Frameworks**: Spring Boot 3.4.x, Spring Data JPA, Spring AI
- **Database**: Snowflake (JDBC)
- **Messaging**: RabbitMQ (Spring AMQP)
- **External APIs**: OpenAI API
- **Build Tool**: Gradle (Multi-module project)
- **Utility**: Lombok, Apache POI, Jackson, SpringDoc

## Project Architecture
The project follows a modular architecture:
- **Root Project (`apn`)**: Main application logic and service layer.
- **`queryprocessor`**: Handles complex data fetching and processing logic.
- **`snowplug`**: Snowflake-specific utilities and configuration.
- **`errorhandler`**: Shared error handling and exception management.

## Getting Started

### Prerequisites
- **JDK 21**
- **Gradle** (Wrapper included)
- **RabbitMQ** (Local or Remote instance)
- **Snowflake Account** (With appropriate permissions)
- **OpenAI API Key**

### Configuration
Update `src/main/resources/application.yaml` or set the following environment variables:
- `SNOWFLAKE_DATABASE_URL`
- `SNOWFLAKE_DATABASE_NAME`
- `SNOWFLAKE_USERNAME`
- `SNOWFLAKE_PASSWORD`
- `OPENAI_API_KEY`
- `RABBITMQ_HOST` (Defaults to `localhost`)

### Running the Application
To run the application locally:
```bash
./gradlew bootRun
```

### Accessing API Documentation
Once the application is running, you can access the Swagger UI at:
`http://localhost:8080/apn/swagger-ui.html`

## Monitoring
- **Health Check**: `http://localhost:8080/apn/actuator/health`
- **Metrics**: `http://localhost:8080/apn/actuator/prometheus`
