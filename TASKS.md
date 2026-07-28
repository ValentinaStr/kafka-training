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
