
---

# 🛵 Ridfix Backend: E-commerce Engine

**Ridfix** is a professional RESTful API developed for a specialized scooter spare parts e-commerce. It features a robust architecture designed to handle complex inventories, secure transactions, and seamless cloud integrations.

---

## 🛠️ Architecture & Tech Stack

### 💾 Core Technologies

* **Java 21 & Spring Boot 3.4.2**: For a reactive and modern backend.
* **PostgreSQL**: Relational database for structured data integrity.
* **Hibernate/JPA**: Implementing a **12-table schema** with advanced inheritance.

### 🏛️ Design Patterns

* **DTO Pattern**: Full separation between Database Entities and API Responses for maximum security.
* **RBAC (Role-Based Access Control)**: 3 levels of security (`CUSTOMER`, `STAFF`, `ADMIN`). 


* **Inheritance Strategy**: Used JOINED inheritance strategy for the Product entity. This ensures a normalized database schema where SparePart and Accessory specific attributes are stored in separate tables, ensuring data integrity while maintaining a unified catalog interface.
---

## 📂 Project Directory Structure

```text
backend/
├── env.properties                 # Local secrets & API keys
├── pom.xml                        # Maven dependencies & build config
├── postman/                       # JSON Collections for API testing
└── src/main/java/it/ridfix/backend/
    ├── entities/                  # 10-table Relational Schema
    ├── repositories/              # Spring Data JPA Interfaces
    ├── services/                  # Business logic & External Adapters
    ├── controllers/               # REST Endpoints (/api/v1)
    ├── security/                  # JWT Filtering & Security Config
    └── exceptions/                # Global Exception Handling Logic

```

---

## 🚀 Key Business Features

* **🛒 Transactional Checkout**: Uses @Transactional and **Pessimistic Locking** (SELECT FOR UPDATE) to ensure robust concurrency control on product stock levels during checkout.
* **🖼️ Cloudinary Integration**: Automated image processing for user profiles and product catalogs.
* **📧 Fail-Safe Messaging**: Integrated with **Mailgun**. The system uses a "best-effort" approach: an email failure will not block the completion of a successful order.
* **🔍 Advanced Catalog**: Polymorphic search for parts by OEM code or compatibility.

---

## ⚙️ Installation & Local Setup

### 1. Prerequisites

* JDK 21
* PostgreSQL 15+

### 2. Environment Configuration

The application is pre-configured to work out-of-the-box with a local PostgreSQL instance. You can override defaults using Environment Variables:

| Variable | Default Value | Description |
| :--- | :--- | :--- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/ridfix_db` | Database connection string |
| `DB_USER` | `postgres` | Your PostgreSQL username |
| `DB_PASSWORD` | `Allahou99!` | Your PostgreSQL password |
| `RIDFIX_JWT_SECRET` | *(Auto-generated default)* | 64-char secret for HS512 |
```properties
PG_DB_NAME=ridfix_db
PG_USERNAME=your_postgres_user
PG_PASSWORD=your_postgres_password
JWT_SECRET=your_secret_key_64_chars_long
CLOUDINARY_URL=cloudinary://key:secret@name
MAILGUN_API_KEY=your_mailgun_api_key

```

### 3. Launching the Server

```bash
# Using the Maven Wrapper
./mvnw clean spring-boot:run

```

The API will be available at: `http://localhost:3001/api`

---

## 🧪 Testing & Validation
To test the API, import the JSON file located in /postman into your Postman Desktop app. Ensure you run the 'Login' request first to populate the accessToken variable.

A comprehensive **Postman Collection** is provided in the `/postman` directory. It includes:

* Pre-configured environments.
* Authentication flows (Login -> Token extraction).
* Test cases for Admin-only routes.

---
