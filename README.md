# 🛵 Ridfix Backend – Scooter Spare Parts E-Commerce API

**Ridfix Backend** is a REST API developed with **Spring Boot** for managing an e-commerce platform focused on **scooter spare parts and accessories**.

The project is designed to demonstrate:

* secure API design
* structured domain modeling
* transactional business logic
* role-based access control
* integration with external services

It is intended as a **backend-only application**, consumable by any frontend or API client.

---

## 🛠️ Tech Stack

* **Java 21**
* **Spring Boot 3.4.2**
* **Spring Security** (JWT, RBAC)
* **Spring Data JPA / Hibernate**
* **PostgreSQL**
* **Maven**

---

## 🏗️ Architecture

### 🔐 Security

* **Stateless JWT authentication**
* **RBAC (Role-Based Access Control)** with three roles:

  * `CUSTOMER`
  * `STAFF`
  * `ADMIN`
* Endpoint protection via `@PreAuthorize`
* Stateless `SecurityFilterChain`
* **CORS configurable via `application.properties`** (not hardcoded)

### 🧱 Domain Model

The domain is modeled using multiple related entities:

* User
* Address
* Product *(abstract)*

  * SparePart
  * Accessory
* Category
* Brand
* Order
* OrderItem
* Payment
* Review
* InventoryMovement

Relationships are explicitly modeled to reflect real e-commerce constraints.

### 🧬 Inheritance Strategy

The product catalog uses **JPA JOINED inheritance**:

* `products` table for shared fields
* `spare_parts` and `accessories` tables for subtype-specific attributes

This strategy was chosen to preserve **normalization and clear subtype boundaries**, at the cost of additional joins.

### 🔄 Transactions & Concurrency

* Checkout logic handled with `@Transactional`
* Atomic stock reduction
* **Pessimistic locking** on products during order creation (`SELECT FOR UPDATE`)
* **Optimistic locking** via `@Version` on the Product entity

---

## 📂 Project Structure

```
backend/
├── pom.xml
├── README.md
├── postman/
│   ├── Ridfix_Backend.postman_collection.json
│   └── Ridfix_Env_Local.json
└── src/main/java/it/ridfix/backend/
    ├── config/        # Security, Beans, CORS
    ├── controllers/  # REST endpoints (/api/**)
    ├── dto/          # Request / Response DTOs
    ├── entities/     # JPA entities
    ├── repositories/# Spring Data JPA
    ├── services/     # Business logic
    ├── security/     # JWT, UserDetails, filters
    ├── external/     # Cloudinary, Mailgun
    ├── seed/         # DataSeeder (dev only)
    └── exceptions/   # Global exception handling
```

---

## 🚀 Main Features

### 👤 Authentication & Users

* User registration and login with JWT
* Profile retrieval (`/auth/me`, `/users/me`)
* User data update
* Profile image upload (multipart)

### 🏠 Addresses

* Shipping and billing address management
* `User → Addresses` relationship
* Default address support

### 📦 Catalog

* Category and brand CRUD (**ADMIN only**)
* Product CRUD (**ADMIN only**)
* Search with filters:

  * text
  * category
  * brand
  * price range
  * availability
* Pagination and sorting

### 🛒 Orders

* Transactional order creation
* Address snapshot stored on the order
* Stock handling with inventory movements
* Order lifecycle (`CREATED`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELLED`)
* User order history
* Order status updates (**STAFF / ADMIN**)

### ⭐ Reviews

* One review per user per product
* **Verified Purchase enforcement** (only buyers can review)
* Rating aggregation (average and count)

### 📊 Admin Statistics

* Aggregation endpoints:

  * Top-selling products (JPQL `GROUP BY` + `SUM`)

---

## ☁️ External Services

### Cloudinary

* Product and profile image uploads
* Configurable via environment variables
* File type and size validation

### Mailgun

* Optional integration
* Disabled by default
* **Fail-soft behavior**: email errors do not block order creation

---

## ⚙️ Configuration

### Environment Variables

| Variable              | Description                      |
| --------------------- | -------------------------------- |
| `DB_URL`              | PostgreSQL JDBC URL              |
| `DB_USER`             | Database username                |
| `DB_PASSWORD`         | Database password                |
| `RIDFIX_JWT_SECRET`   | JWT secret (required)            |
| `RIDFIX_CORS_ORIGINS` | Allowed CORS origins             |
| `CLOUDINARY_*`        | Cloudinary credentials           |
| `MAILGUN_*`           | Mailgun configuration (optional) |

---

## ▶️ Run the Application

### Windows

```powershell
.\mvnw clean spring-boot:run
```

### Linux / macOS

```bash
./mvnw clean spring-boot:run
```

API available at:
**[http://localhost:3001/api](http://localhost:3001/api)**

---

## 👥 Seeded Users (DEV ONLY)

The following users are automatically created **only in development mode** by the `DataSeeder`:

| Role     | Email                                                 | Password      |
| -------- | ----------------------------------------------------- | ------------- |
| ADMIN    | [admin@ridfix.local](mailto:admin@ridfix.local)       | Admin1234!    |
| STAFF    | [staff@ridfix.local](mailto:staff@ridfix.local)       | Staff1234!    |
| CUSTOMER | [customer@ridfix.local](mailto:customer@ridfix.local) | Customer1234! |

*(For development and testing purposes only)*

---

## 🧪 API Testing (Postman)

The repository includes a complete **Postman Collection and Environment**:

```
/postman/Ridfix_Backend.postman_collection.json
/postman/Ridfix_Env_Local.json
```

### How to run tests

1. Import both files into Postman.
2. Select the **Local** environment.
3. Run login requests to generate JWT tokens:

  * ADMIN → `tokenAdmin`
  * CUSTOMER → `tokenCustomer`
  * STAFF → `tokenStaff`
4. Execute requests by role-based folders.

⚠️ Tokens are intentionally **not included** in the environment for security and reproducibility.

---

## ✅ Project Status

* ✔ Clean and coherent domain model
* ✔ No hardcoded secrets in the repository
* ✔ Secure, stateless authentication
* ✔ Transactional and concurrency-safe business logic
