# Banking Core Application

## Description
A small core banking solution that manages accounts, balances, and transactions.  
Built for Tuum assignment using modern Java/Spring Boot stack with RabbitMQ event publishing.

## Technologies
- Java 17, Spring Boot 3.4.4, MyBatis, Gradle
- PostgreSQL, RabbitMQ, Testcontainers, JUnit

## How to run with Docker (recommended)

### Prerequisites
- Docker Desktop installed and running
- Ports `8080`, `5433`, `5672`, `15672` must be free

### Steps

```bash
# Clone the repository
git clone https://github.com/Elisbett/Tuum_Summer_Internship_2026.git
cd Tuum_Summer_Internship_2026

# Start all services (PostgreSQL, RabbitMQ, and the app)
docker-compose up -d --build

# Verify containers are running
docker ps
```

The application will be available at: `http://localhost:8080`

## REST API Endpoints

| Method | URL | Description |
|--------|-----|-------------|
| `POST` | `/accounts` | Create a new account with balances in multiple currencies |
| `GET` | `/accounts/{id}` | Get account details and current balances |
| `POST` | `/accounts/{id}/transactions` | Create a transaction (IN or OUT) |
| `GET` | `/accounts/{id}/transactions` | Get transaction history for an account |

### Example requests (using `curl`)

#### 1. Create account

```bash
curl -X POST http://localhost:8080/accounts \
  -H "Content-Type: application/json" \
  -d '{"customerId": 1, "country": "Estonia", "currencies": ["EUR", "USD"]}'
```

**Response:**

```json
{
  "accountId": 1,
  "customerId": 1,
  "balances": [
    {"availableAmount": 0, "currency": "EUR"},
    {"availableAmount": 0, "currency": "USD"}
  ]
}
```

#### 2. Get account

```bash
curl http://localhost:8080/accounts/1
```

#### 3. Create IN transaction (deposit)

```bash
curl -X POST http://localhost:8080/accounts/1/transactions \
  -H "Content-Type: application/json" \
  -d '{"amount": 150.50, "currency": "EUR", "direction": "IN", "description": "Salary"}'
```

#### 4. Create OUT transaction (withdrawal)

```bash
curl -X POST http://localhost:8080/accounts/1/transactions \
  -H "Content-Type: application/json" \
  -d '{"amount": 30.00, "currency": "EUR", "direction": "OUT", "description": "Coffee"}'
```

#### 5. Get transaction history

```bash
curl http://localhost:8080/accounts/1/transactions
```

### Error responses
- `400 Bad Request` — invalid currency, insufficient funds, missing description, negative amount
- `404 Not Found` — account does not exist


## RabbitMQ Event Publishing

All account creation and balance update operations are published to RabbitMQ.

- **Management UI:** `http://localhost:15672` (guest/guest)
- **Exchange name:** `tuam.account.events`
- **Events:**
  - `AccountCreated` (routing key: `account.created`)
  - `BalanceUpdated` (routing key: `balance.updated`)

To verify events:

1. Open RabbitMQ UI → `Exchanges` → `tuam.account.events`
2. Create a queue and bind it with `#` (all messages)
3. Perform any account operation
4. Check the queue — messages will appear


## Integration Tests & Coverage

- Tests use **Testcontainers** (automatically start PostgreSQL and RabbitMQ in Docker)
- Framework: JUnit 5, Spring Boot Test, TestRestTemplate
- **Test coverage:** ~94% (exceeds the 80% requirement)

Run tests:

```bash
./gradlew clean test
```

Generate coverage report:

```bash
./gradlew jacocoTestReport
```

Open the report: `build/reports/jacoco/test/html/index.html`


## Performance Estimate

**On a typical development machine (8 cores, 16GB RAM):**

- Estimated throughput: **100–200 transactions per second**
- Bottlenecks: database writes (PostgreSQL), RabbitMQ publishing
- With optimisations (connection pooling, batch inserts) — up to ~300 TPS

> *Note: Actual performance may vary depending on hardware and load.*


## Horizontal Scaling Considerations

To scale the application horizontally:

1. **Make the service stateless**
   - No session affinity required
   - Any instance can handle any request

2. **Place a load balancer in front** (e.g., Nginx, AWS ALB)
   - Distributes traffic across multiple app instances

3. **Scale the database**
   - Use PostgreSQL read replicas for queries
   - Consider connection pooling (HikariCP already configured)

4. **Cluster RabbitMQ**
   - Use a RabbitMQ cluster for high availability of messages
   - Configure mirrored queues to prevent data loss

5. **Container orchestration**
   - Deploy with Kubernetes or Docker Swarm
   - Auto‑scale based on CPU/memory metrics

6. **Add distributed tracing**
   - Spring Cloud Sleuth + Zipkin for request tracking across services


## AI Usage

ChatGPT was used as a coding assistant for:
- Initial project setup and Gradle configuration
- MyBatis mapper creation and debugging
- RabbitMQ event publishing logic
- Dockerfile and docker-compose.yml adjustments
- Writing integration tests with Testcontainers
- Generating the README structure

All code was reviewed, tested, and adapted by me to meet the assignment requirements.

## Project Structure (key folders)

```
src/main/java/com/tuam/bankingcore/
├── config/           # RabbitMQ, MyBatis configuration
├── controller/       # REST endpoints
├── dto/              # Request/response DTOs
├── exception/        # Global error handling
├── mapper/           # MyBatis mappers (annotations)
├── model/            # Domain entities (Account, Balance, Transaction)
└── service/          # Business logic + RabbitMQ events
src/main/resources/
├── application.yml   # Application properties
└── schema.sql        # Database schema definition

src/test/java/...     # Integration tests with Testcontainers
```

## License
This project was created as a technical assignment.

**Author:** Elisabeth
**Date:** April 2026
