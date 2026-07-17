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
 
Prerequisites
 
Software	Version	Purpose
Java	21 LTS	Runtime for Spring Boot and Kafka
IntelliJ IDEA	
Community
	IDE
Git	Latest	Clone the repository
Gradle	8.x (or use Gradle Wrapper)	Build tool
Apache Kafka	3.9+ (KRaft mode)	Message Broker
PostgreSQL	
16+(or mysql)
	Database
 
 
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
 
 