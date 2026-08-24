```
                     REST
                      |
                Order Service
                      |

             Kafka: order-events
       (OrderCreated/OrderUpdated/OrderCancelled)
                      |
        ---------------------------------
        |                               |
Inventory Service             Notification Service
        |
Kafka: inventory-result
        |
Order Service
```

## Message flow

1. `POST /orders` on **order-service** — validates the request, saves the order to Postgres (status `NEW`), then publishes an `OrderEvent` (`eventType=CREATED`), keyed by `customerId`, to the `order-events` topic (only after the DB transaction commits).
2. Two independent consumer groups read every `order-events` message (pub/sub fan-out), each acting only on the event types it cares about:
   - **inventory-service** acts only on `CREATED` events — checks quantity against a threshold and publishes an `InventoryResultEvent` (`AVAILABLE` or `OUT_OF_STOCK`) to the `inventory-result` topic. `UPDATED`/`CANCELLED` events are ignored.
   - **notification-service** reacts to all three event types (`CREATED`/`UPDATED`/`CANCELLED`) with a different pretend email per type (prints to its console) — it doesn't publish anything back.
3. **order-service** consumes `inventory-result`, updates the order's status in Postgres (`NEW` → `AVAILABLE`/`OUT_OF_STOCK`), then publishes another `OrderEvent` back to `order-events` (`eventType=UPDATED` if `AVAILABLE`, `CANCELLED` if `OUT_OF_STOCK`).

## Kafka keys & partitions

`order-events` has 3 partitions (declared by `KafkaTopicConfig`), keyed by `customerId` (`OrderEventProducer.send`). Kafka hashes the key to pick a partition and only guarantees ordering within a partition, so all events for one customer always land on the same partition and stay in send order.

Observe in Kafka UI (http://localhost:8090): open `order-events` → **Messages**, check the `Partition`/`Key` columns — same `customerId` always maps to the same partition.

## Prerequisites

| Software | Version | Purpose |
|---|---|---|
| Java | 21 LTS | Runtime for Spring Boot and Kafka |
| IntelliJ IDEA | Community | IDE |
| Git | Latest | Clone the repository |
| Gradle | 8.x (or use Gradle Wrapper) | Build tool |
| Docker Desktop | Latest | Runs Postgres / Kafka / Kafka UI / pgAdmin via Compose |
| Apache Kafka | 3.9 (KRaft mode) | Message Broker — runs as a container (`bitnamilegacy/kafka:3.9`), no local install needed |
| PostgreSQL | 15+ (docker-compose uses `postgres:15-alpine`) | Database |

Tasks — see [TASKS.md](TASKS.md)

## Running the project

You need to start the infrastructure and all three services:

### 1. Infrastructure (Postgres + Kafka + Kafka UI + pgAdmin) — via Docker Compose

Make sure Docker Desktop is running first.

```bash
docker compose up -d
```

Starts:
- **PostgreSQL** — `localhost:5432`, database `orderdb`, user/password `postgres`/`postgres`
- **Kafka** — `localhost:9092` (KRaft mode, single broker, no ZooKeeper)
- **Kafka UI** — http://localhost:8090 (web UI for browsing topics and messages)
- **pgAdmin** — http://localhost:5050, login `admin@admin.com` / `admin`
  (after first login, manually add a server: Host `postgres`, Port `5432`, DB `orderdb`, User/Password `postgres`/`postgres`)

Kafka has no persistent volume — topics are lost whenever the `kafka` container is recreated (including `docker compose down` + `up`, not just `-v`). `order-events.DLT` and `inventory-result` are auto-created on first publish/subscribe; `order-events` is declared explicitly by order-service on startup (3 partitions — see [Kafka keys & partitions](#kafka-keys--partitions)).

Stop: `docker compose down` (Postgres data persists via its volume; Kafka topics do not), or `docker compose down -v` (also wipes Postgres data).

### 2. Services

```bash
cd order-service
./gradlew bootRun
```

```bash
cd inventory-service
./gradlew bootRun
```

```bash
cd notification-service
./gradlew bootRun
```

### Verification

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 15, "product": "Laptop", "quantity": 2}'
```

Expected: `202 Accepted` with `orderId`, a row in the `orders` table (status `NEW`), `Processing order: ...` in the `inventory-service` console, and `Email sent to customer ...` in the `notification-service` console.
