# ProjectA — Production-Grade Microservices Upgrade

## Architecture Overview

```
Internet
    │
    ▼
┌─────────────────────────────────────────────────┐
│              API Gateway  :8080                 │
│   Spring Cloud Gateway + Keycloak OAuth2        │
│   Rate Limiting (Redis) + Circuit Breaker       │
└────┬──────────┬──────────┬─────────────┬────────┘
     │          │          │             │
     ▼          ▼          ▼             ▼
  User       Order      Payment    Notification
 Service    Service     Service      Service
  :8081      :8082       :8083        :8084
     │          │          │             │
     └──────────┴────┬─────┘             │
                     │                   │
          ┌──────────▼───────────┐        │
          │       Kafka          │◄───────┘
          │  (Event Streaming)   │
          └──────────────────────┘
                     │
          ┌──────────▼───────────┐
          │      RabbitMQ        │
          │  (Reliable Delivery  │
          │   for Payments)      │
          └──────────────────────┘
```

## Services

| Service              | Port  | Description                              |
|----------------------|-------|------------------------------------------|
| API Gateway          | 8080  | Routes, auth, rate limiting              |
| User Service         | 8081  | Registration, profiles, Keycloak sync    |
| Order Service        | 8082  | Order lifecycle, Kafka events            |
| Payment Service      | 8083  | Payment processing, RabbitMQ publish     |
| Notification Service | 8084  | Email / SMS / Push / In-App              |

## Infrastructure

| Service      | Port  | Description                              |
|--------------|-------|------------------------------------------|
| Keycloak     | 8180  | Identity & Access Management             |
| Kafka        | 29092 | Event streaming (orders, users)          |
| RabbitMQ     | 15672 | Message queue (payments, guaranteed MQ)  |
| PostgreSQL   | 5432  | Per-service databases                    |
| Redis        | 6379  | Session cache + rate limiting            |
| Prometheus   | 9090  | Metrics collection                       |
| Grafana      | 3000  | Dashboards                               |
| Kafka UI     | 8090  | Kafka management UI                      |

## Kafka Topics

| Topic                | Producer        | Consumer             |
|----------------------|-----------------|----------------------|
| `order.placed`       | Order Service   | Notification Service |
| `order.confirmed`    | Order Service   | Notification Service |
| `order.shipped`      | Order Service   | Notification Service |
| `order.delivered`    | Order Service   | Notification Service |
| `order.cancelled`    | Order Service   | Notification Service |
| `payment.success`    | Payment Service | Notification Service |
| `payment.failed`     | Payment Service | Notification Service |
| `payment.refunded`   | Payment Service | Notification Service |
| `user.registered`    | User Service    | Notification Service |
| `user.password-reset`| User Service    | Notification Service |

## RabbitMQ Queues

| Queue                         | Purpose                         |
|-------------------------------|---------------------------------|
| `payment.notification.queue`  | Payment events → Notifications  |
| `notification.dead-letter.queue` | Failed notifications (DLQ)   |

## Notification Channels

- **Email** — via SMTP (Gmail / SES / SendGrid)
- **SMS** — via Twilio
- **Push** — via Firebase Cloud Messaging (FCM)
- **In-App** — stored in DB, served via REST

## Quick Start

```bash
# 1. Clone and copy env
cp .env.example .env
# Fill in .env with real credentials

# 2. Start all services
docker compose up -d

# 3. Wait for Keycloak (~60s), then access:
#    API Gateway:   http://localhost:8080
#    Keycloak:      http://localhost:8180
#    Kafka UI:      http://localhost:8090
#    RabbitMQ UI:   http://localhost:15672
#    Grafana:       http://localhost:3000
```

## Security

- All services secured with Keycloak JWT (RS256)
- Role-based access: `USER`, `ADMIN`, `SUPPORT`
- Gateway enforces auth; services trust gateway headers
- Secrets managed via `.env` (never committed)
- Non-root Docker containers
- TLS termination at load balancer (prod)
