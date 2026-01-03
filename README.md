# CQRS-Axon-Implementation

Learning-grade CQRS + Event Sourcing example using Spring Boot 4.0.1 and Axon 5.0.1. The repo is a multi-module Maven build with two Spring Boot apps and shared contracts.

Modules:
- order-contracts (commands, events, query DTOs)
- order-command-service (write side)
- order-query-service (read side)

## Prerequisites

- Java 21+
- Docker (for Axon Server, Postgres, Testcontainers)

## Local infrastructure

```bash
docker compose up -d
```

Axon Server UI: http://localhost:8024

## Run services

```bash
mvn -pl order-command-service spring-boot:run
mvn -pl order-query-service spring-boot:run
```

## Run tests

```bash
mvn test
```

Integration tests use Testcontainers and require Docker.

## Example curl commands

```bash
# place order
curl -X POST localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"orderId":"order-1","customerId":"cust-1"}'

# add item
curl -X POST localhost:8081/orders/order-1/items \
  -H "Content-Type: application/json" \
  -d '{"sku":"SKU-1","qty":2}'

# confirm payment
curl -X POST localhost:8081/orders/order-1/payment/confirm \
  -H "Content-Type: application/json" \
  -d '{"paymentId":"pay-1"}'

# ship order
curl -X POST localhost:8081/orders/order-1/ship \
  -H "Content-Type: application/json" \
  -d '{"shipmentId":"ship-1"}'

# query order
curl localhost:8082/orders/order-1

# list customer orders
curl localhost:8082/customers/cust-1/orders
```
