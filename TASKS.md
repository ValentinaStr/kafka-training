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