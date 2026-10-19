Task 1. Create Producer Service
 
Requirements
Create Spring Boot application called order-service.
Implement REST endpoint
POST /orders
Request
{
  "customerId": 15,
  "product": "Laptop",
  "quantity": 2}
Generate
orderId (UUID)
createdTime
Publish message to Kafka topic
order-created
No database yet.
 
Task 2. Create Consumer Service
 
Create another application
inventory-service
Consume
order-created
For every message
Print
Processing order:
OrderId: xxx
Product: Laptop
Quantity: 2
Return nothing.

Task 3. Add Validation
 
Requirements
If quantity <= 0 or product == null
REST should return 400 Bad Request
Use Bean Validation
@NotNull
@Positive

Task 4. Introduce Database
 
Requirements
Add PostgreSQL.
Store every created order.
Table orders
Columns
id
customer_id
product
quantity
status
created_at
Status initially NEW
Publish Kafka event only after successful save.

Task 5. Update Order Status
 
Requirements
Inventory service should simulate stock.
If quantity <=5 status AVAILABLE
otherwise OUT_OF_STOCK
Publish event inventory-result
Example
{ "orderId":"...", "status":"AVAILABLE"}

Task 6. Consume Response
 
Requirements
Order Service consumes inventory-result
Update order status in DB.
Statuses
NEW
AVAILABLE
OUT_OF_STOCK

Task 7. Retry & Error Handling
Randomly throw exception
if (Random.nextBoolean())
    throw new RuntimeException();
Configure
retries
backoff
Message eventually succeeds.
 
Task 8. Dead Letter Topic (DLT)
If message fails after retries
Move it into
order-created.DLT
Create consumer
dlt-monitor
Print failed messages.
Skills
DLT
DeadLetterPublishingRecoverer

Task 9. Idempotency
Duplicate events may arrive.
Prevent processing same
orderId
twice.
Possible implementation
processed_messages
table.
 
Task 10. Multiple Consumers
Create
notification-service
Consumes same event.
Pretend sending email.
Example
Email sent to customer
Shows pub/sub.

Task 11. Multiple Event Types
Introduce
OrderCreated
OrderCancelled
OrderUpdated
Use one topic
order-events
Handle different event types.

Task 11, order-events replaces order-created. Order Service will send all order events (OrderCreated, OrderUpdated, OrderCancelled) to order-events.
Both Inventory Service and Notification Service will consume this topic, but they will use separate consumer groups. Each service should process only the events it needs.
The order-created topic was only for the previous tasks.
 
Task 12. Kafka Keys & Partitions
Send
customerId
as Kafka key.
Observe
partition distribution
ordering
Explain why messages for one customer stay ordered.

Task 13. Consumer Groups
Run
inventory-service
3 instances.
Observe load balancing.
Then start another consumer group.
Observe fan-out.

Task 14. Integration Testing
Use
@EmbeddedKafka
or Testcontainers.
Verify
POST /orders
actually produces Kafka event.
Verify consumer processes it.
Task 15. Observability
Add
Micrometer
Prometheus
Health endpoint
Logging correlation ID

Task 16. Transactional Messaging
Guarantee
DB saved
Kafka message published
Discuss
Transactional Outbox Pattern
Why dual writes are dangerous
No need to fully implement Outbox initially—just understand the problem.

Task 17 — Global Exception Handling
 
Currently, different controllers/services may handle exceptions differently.
Create a centralized:
@RestControllerAdvice
public class GlobalExceptionHandler
Handle things like:
OrderNotFoundException
InvalidOrderException
ValidationException
Return a consistent response:
{
  "timestamp": "...",
  "status": 404,
  "error": "ORDER_NOT_FOUND",
  "message": "Order with id 123 was not found"
}
Requirements
No controller should contain repetitive exception handling.
Validation errors should return meaningful messages.
HTTP status codes should be appropriate.
They learn
@RestControllerAdvice
@ExceptionHandler
REST error design
Clean API design
Task 18 — Swagger / OpenAPI
 
Add OpenAPI documentation.
Document:
POST /orders
GET /orders/{id}
GET /orders
Include:
request examples
response examples
possible HTTP status codes
descriptions
They should be able to open Swagger UI and test the API.
Bonus
Document Kafka events as well.
Task 19 — Pagination & Filtering
This is a good real-world REST task.
Currently:
GET /orders
returns everything.
Change it to:
GET /orders?page=0&size=20
and support:
GET /orders?status=AVAILABLE
or:
GET /orders?page=0&size=20&status=AVAILABLE
Response:
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 153,
  "totalPages": 8
}
They learn
Spring Data pagination
Query parameters
API design
Database performance considerations
Bonus
Add sorting:
GET /orders?page=0&size=20&sort=createdAt,desc
Task 20 — MapStruct
Now that they have several DTOs, introduce MapStruct.
For example:
OrderRequest
      ↓
Order
      ↓
OrderResponse
Instead of manually doing:
order.setId(entity.getId());
order.setStatus(entity.getStatus());
order.setCreatedAt(entity.getCreatedAt());
create:
@Mapper(componentModel = "spring")
public interface OrderMapper {
    Order toEntity(OrderRequest request);
    OrderResponse toResponse(Order order);
}
 
 
Task 21 — Replace JSON with Avro
Now we start making Kafka more advanced.
Currently:
Kafka
   |
   | JSON
   v
OrderCreated
Change events to Avro.
For example:
OrderCreated.avsc
containing:
{
  "type": "record",
  "name": "OrderCreated",
  "fields": [
    {
      "name": "orderId",
      "type": "string"
    },
    {
      "name": "customerId",
      "type": "long"
    },
    {
      "name": "product",
      "type": "string"
    },
    {
      "name": "quantity",
      "type": "int"
    }
  ]
}
Generate Java classes from the schema.
 
Task 22 — Schema Registry
Now make Task 21 more realistic.
Add Schema Registry.
Architecture becomes:
                 Schema Registry
                       ↑
                       |
Order Service → Kafka ←→ Inventory Service
                  ↑
                  |
            Avro messages
The services should register/use the Avro schemas.
Then introduce schema evolution.
For example, add:
customerEmail
to OrderCreated.
The existing consumers should continue working.
Then discuss:
What happens if we remove a field?
What happens if we rename a field?
What does backward compatibility mean?
Task 23 — JWT Authentication
Now secure the REST API.
Add Spring Security + JWT.
For example:
POST /auth/login
returns:
{
  "token": "eyJ..."
}
Then:
GET /orders
Authorization: Bearer eyJ...
Without authentication:
401 Unauthorized
Add roles
For example:
CUSTOMER
ADMIN
Customer:
GET /orders
POST /orders
Admin:
GET /orders
DELETE /orders
 
 
