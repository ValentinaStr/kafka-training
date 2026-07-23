                     REST

                      |

                Order Service

                      |

             Kafka: order-created

                      |

        ---------------------------------

        |                               |

Inventory Service             Notification Service

        |

Kafka: inventory-result

        |

Order Service
 
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

You need to start 2 components:

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

Kafka has no persistent volume — topics are lost whenever the `kafka` container is recreated (including `docker compose down` + `up`, not just `-v`). Create the topic once per restart:
```bash
docker exec order-service-kafka kafka-topics.sh --create --if-not-exists --topic order-created --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
```
(or create it via Kafka UI instead)

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

### Verification

```bash
curl -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId": 15, "product": "Laptop", "quantity": 2}'
```

Expected: `202 Accepted` with `orderId`, a row in the `orders` table (status `NEW`), and in the `inventory-service` console — `Processing order: ...`.
