# 🎧 NextCallDev – Microservices-Based Meeting Platform

NextCallDev is a distributed meeting and collaboration platform built with a modern microservices architecture.  
It leverages **Spring Boot**, **Node.js**, **Docker**, and a full set of cloud-native patterns to provide a scalable, resilient, and real-time communication system for online meetings.

---

## 🚀 Overview

NextCallDev enables users to create, join, and manage virtual meetings.  
The system is split into multiple independently deployable services, each responsible for a specific domain, and orchestrated through a robust microservices ecosystem.

The platform implements a wide range of microservice design patterns such as:

- **API Gateway pattern**
- **Service Registry & Service Discovery**
- **Centralized Configuration Server**
- **Event-driven communication**
- **Dedicated WebSocket services**
- **Circuit Breakers / Resilience patterns**
- **Externalized configuration per environment**
- **Decoupled deployment with Docker**

---

## 🧱 Architecture

The project is composed of multiple microservices, including:

### **Spring Boot microservices**
- Authentication service  
- Meetings service  
- Notifications service  
- User profile service  
- WebSocket signaling service  
- Config Server  
- Service Registry (Eureka / alternative)  

### **Node.js microservice**
- A dedicated service for real-time or event-intensive workloads (such as meeting signaling or chat handling)

---

## 🔌 Core Components & Technologies

### **API Gateway**
- Central entry point for all clients  
- Handles routing, filtering, and security  
- Built with Spring Cloud Gateway  

### **Service Registry**
- Provides service discovery  
- Enables dynamic scaling and decoupled communication  
- Eureka or similar registry  

### **Config Server**
- Stores external configuration for all microservices  
- Configurations are kept in a **separate Git repository**  
- Supports environment-based config loading  

### **WebSockets**
- Real-time communication for meetings  
- Used for events such as speaking, joining, leaving, chat messages, etc.

### **Dockerized Infrastructure**
- Each microservice includes its own Dockerfile  
- Fully orchestratable using Docker Compose or containers in production  
- Enables isolated deployments and fast scalability

---

## 🧩 Microservice Design Patterns Implemented

This project applies a wide variety of microservices patterns.  
(Some examples — feel free to expand based on your internal implementation.)

- **API Gateway Pattern**
- **Service Registry & Discovery**
- **Centralized Configuration**
- **Client-side Load Balancing**
- **Circuit Breaker / Retry / Fallback**
- **Distributed Logging**
- **Database per Microservice**
---

## 🐳 Docker Setup

All services are fully containerized:

```bash
# Build all images
docker-compose build

# Start the entire microservices ecosystem
docker-compose up
