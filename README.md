# Inventory Service

## 🎯 Purpose

The Inventory Service is a **business microservice** responsible for managing the product catalog and inventory. It provides RESTful APIs for creating, reading, updating, and deleting products, as well as managing stock levels.

## 🔌 Port

- **Default Port:** `8081` (configured via Config Server)
- **Direct Access:** http://localhost:8081
- **Via Gateway:** http://localhost:8080/inventory-service

## 🛠️ Tech Stack

- **Framework:** Spring Boot 3.5.7
- **Web:** Spring Web (REST)
- **Data Access:** Spring Data JPA
- **Database:** H2 (in-memory, development) / PostgreSQL (production, planned)
- **Service Discovery:** Spring Cloud Netflix Eureka Client
- **Configuration:** Spring Cloud Config Client
- **Validation:** Bean Validation (Hibernate Validator)
- **Utilities:** Lombok
- **Monitoring:** Spring Boot Actuator
- **Build Tool:** Maven

## 📦 Dependencies

**Required Services:**
1. **Service Registry** (Eureka) - For service registration
2. **Config Server** - For pulling configuration from Git repository

**Optional Services:**
- **API Gateway** - For routing client requests

## 📐 Architecture

The service follows a **layered architecture**:

```
┌─────────────────────────────────────┐
│         Controller Layer            │
│  (ProductController.java)           │
│  - Handles HTTP requests            │
│  - Validates input                  │
│  - Returns HTTP responses           │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│          Service Layer              │
│  (ProductService.java)              │
│  - Business logic                   │
│  - Transaction management           │
│  - Orchestration                    │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│        Repository Layer             │
│  (ProductRepository.java)           │
│  - Data access                      │
│  - JPA/Hibernate                    │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│          Database (H2)              │
│  - In-memory storage                │
│  - Products table                   │
└─────────────────────────────────────┘
```

## ⚙️ Configuration

### Local Configuration (`application.yml`):

```yaml
spring:
  application:
    name: inventory-service
  config:
    import: "configserver:http://localhost:8888"
  cloud:
    config:
      fail-fast: true
```

### Externalized Configuration (from Config Server):

```yaml
server:
  port: 8081

spring:
  datasource:
    url: jdbc:h2:mem:inventorydb
    driver-class-name: org.h2.Driver
    username: sa
    password:

  h2:
    console:
      enabled: true

  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

## 🚀 How to Run

### Prerequisites

- Java 21 or higher
- Maven 3.8+
- Service Registry running on port 8761
- Config Server running on port 8888

### Running Locally

```bash
# Clone the repository
git clone https://github.com/DanLearnings/ecommerce-inventory-service.git
cd ecommerce-inventory-service

# Ensure Service Registry and Config Server are running
# Then run Inventory Service
mvn spring-boot:run

# Or build and run the JAR
mvn clean package
java -jar target/ecommerce-inventory-service-1.0.0.jar
```

### Running with Docker (Coming Soon)

```bash
docker run -p 8081:8081 \
  -e SPRING_CONFIG_IMPORT=configserver:http://config-server:8888 \
  -e EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://service-registry:8761/eureka/ \
  ghcr.io/danlearnings/ecommerce-inventory-service:latest
```

## 📋 Data Model

### Product Entity

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(unique = true)
    private String sku;
}
```

### Database Schema

```sql
CREATE TABLE products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    quantity INTEGER NOT NULL,
    sku VARCHAR(255) UNIQUE
);
```

## 🔍 API Endpoints

### Base URL

- **Direct:** `http://localhost:8081`
- **Via Gateway:** `http://localhost:8080/inventory-service`

### Endpoints

#### 1. List All Products

```bash
GET /products

# Example
curl http://localhost:8080/inventory-service/products
```

**Response:** `200 OK`
```json
[
  {
    "id": 1,
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1200.00,
    "quantity": 50,
    "sku": "LAP-001"
  }
]
```

#### 2. Get Product by ID

```bash
GET /products/{id}

# Example
curl http://localhost:8080/inventory-service/products/1
```

**Response:** `200 OK` or `404 Not Found`

#### 3. Get Product by SKU

```bash
GET /products/sku/{sku}

# Example
curl http://localhost:8080/inventory-service/products/sku/LAP-001
```

**Response:** `200 OK` or `404 Not Found`

#### 4. Create Product

```bash
POST /products
Content-Type: application/json

# Example
curl -X POST http://localhost:8080/inventory-service/products \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Laptop",
    "description": "High-performance laptop",
    "price": 1200.00,
    "quantity": 50,
    "sku": "LAP-001"
  }'
```

**Response:** `201 Created`
```json
{
  "id": 1,
  "name": "Laptop",
  "description": "High-performance laptop",
  "price": 1200.00,
  "quantity": 50,
  "sku": "LAP-001"
}
```

#### 5. Update Product

```bash
PUT /products/{id}
Content-Type: application/json

# Example
curl -X PUT http://localhost:8080/inventory-service/products/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Gaming Laptop",
    "description": "High-performance gaming laptop",
    "price": 1500.00,
    "quantity": 30,
    "sku": "LAP-001"
  }'
```

**Response:** `200 OK` or `404 Not Found`

#### 6. Delete Product

```bash
DELETE /products/{id}

# Example
curl -X DELETE http://localhost:8080/inventory-service/products/1
```

**Response:** `200 OK` or `404 Not Found`

#### 7. Check Stock Availability

```bash
GET /products/{id}/check-stock?quantity={amount}

# Example
curl "http://localhost:8080/inventory-service/products/1/check-stock?quantity=10"
```

**Response:** `200 OK`
```json
true  // or false
```

## 🧪 Testing with Postman

### Import Collection

A complete Postman collection is available with pre-configured requests for all endpoints.

### Example Test Flow

1. **Create a product** (POST)
2. **List all products** (GET) - Verify product appears
3. **Get specific product** (GET by ID)
4. **Update product** (PUT)
5. **Check stock** (GET check-stock)
6. **Delete product** (DELETE)
7. **Verify deletion** (GET by ID) - Should return 404

## ✅ Health Check

Verify the service is working correctly:

```bash
# 1. Check service health
curl http://localhost:8081/actuator/health

# 2. Verify Eureka registration
# Visit http://localhost:8761 and check for INVENTORY-SERVICE

# 3. Verify Config Server connection
# Check startup logs for: "Located environment: name=inventory-service"

# 4. Test API endpoint
curl http://localhost:8081/products

# 5. Test H2 Console (if enabled)
# Visit http://localhost:8081/h2-console
# JDBC URL: jdbc:h2:mem:inventorydb
# Username: sa
# Password: (empty)
```

## 🐛 Troubleshooting

### Service fails to start - Port 8081 already in use

**Solution:**
```bash
# Windows
netstat -ano | findstr :8081
taskkill /PID <PID> /F

# Linux/Mac
lsof -i :8081
kill -9 <PID>
```

### Service starts on wrong port (8080 instead of 8081)

**Symptom:** Service logs show "Tomcat started on port 8080"

**Solution:**
1. Verify Config Server is running and accessible
2. Check Config Server endpoint: http://localhost:8888/inventory-service/default
3. Ensure `spring.config.import` is set correctly in `application.yml`
4. Remove `optional:` from config import to enforce Config Server connection

### Service not registering with Eureka

**Solution:**
1. Ensure Service Registry is running on port 8761
2. Check `eureka.client.service-url.defaultZone` in configuration
3. Wait 30 seconds for initial registration
4. Review logs for Eureka connection errors

### H2 Console not accessible

**Solution:**
1. Verify `spring.h2.console.enabled: true` in configuration
2. Ensure you're accessing the correct URL: http://localhost:8081/h2-console
3. Use JDBC URL: `jdbc:h2:mem:inventorydb`

### JPA/Hibernate errors on startup

**Solution:**
1. Check entity annotations (`@Entity`, `@Table`, `@Id`)
2. Verify datasource configuration
3. Review logs for specific error messages
4. Ensure H2 dependency is present in `pom.xml`

## 🔮 Future Enhancements

- [ ] **DTOs:** Separate domain models from API contracts
- [ ] **Pagination:** Add pagination for product listing
- [ ] **Filtering & Sorting:** Advanced search capabilities
- [ ] **Global Exception Handling:** Centralized error handling with @ControllerAdvice
- [ ] **Unit Tests:** Comprehensive test coverage
- [ ] **Integration Tests:** Test with real database
- [ ] **PostgreSQL:** Replace H2 with production-grade database
- [ ] **Flyway/Liquibase:** Database migration management
- [ ] **Caching:** Add Redis for frequently accessed products
- [ ] **Search:** Elasticsearch integration for advanced search

## 📚 Additional Resources

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Spring Web (REST) Guide](https://spring.io/guides/gs/rest-service/)
- [Bean Validation Reference](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)

## 🔗 Related Services

- [Service Registry](https://github.com/DanLearnings/ecommerce-service-registry) - Registers with Eureka
- [Config Server](https://github.com/DanLearnings/ecommerce-config-server) - Pulls configuration
- [API Gateway](https://github.com/DanLearnings/ecommerce-api-gateway) - Routes client requests
- [Order Service](https://github.com/DanLearnings/ecommerce-order-service) - Will consume this service (planned)

## 👨‍💻 Maintainer

**Dan Learnings**
- GitHub: [@DanrleyBrasil](https://github.com/DanrleyBrasil)
- Organization: [DanLearnings](https://github.com/DanLearnings)

---

**Last Updated:** October 29, 2025
