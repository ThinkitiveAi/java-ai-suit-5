# Health First Server - Provider Registration Backend

## Overview
This is a secure Spring Boot 3+ backend module for Provider Registration, featuring strong validation, REST APIs, PostgreSQL schema, email verification, and best practices in architecture and security.

## Tech Stack
- Java 17+
- Spring Boot 3+
- Spring Security (bcrypt)
- Spring Data JPA
- PostgreSQL
- Hibernate Validator
- Java MailSender
- Lombok
- JUnit 5
- Maven

## Setup Instructions

### 1. Prerequisites
- Java 17+
- Maven
- PostgreSQL (local or Docker)

### 2. Database Setup
- Create a PostgreSQL database (e.g., `provider_db`).
- Create a user and grant privileges:
  ```sql
  CREATE USER provider_user WITH PASSWORD 'yourpassword';
  CREATE DATABASE provider_db;
  GRANT ALL PRIVILEGES ON DATABASE provider_db TO provider_user;
  ```
- Update `src/main/resources/application.properties` with your DB credentials.

### 3. Email Sender Setup
- The app uses Spring Boot's JavaMailSender.
- For local/dev, use a dummy SMTP (e.g., [MailHog](https://github.com/mailhog/MailHog)) or Gmail SMTP for testing.
- Update `application.properties` with your SMTP config.

### 4. Build & Run
```bash
mvn clean package
java -jar target/health-first-server-1.0-SNAPSHOT.jar
```

### 5. API Endpoint
- `POST /api/v1/provider/register`
- See API docs or controller for request/response format.

### 6. Testing
- Run all tests:
  ```bash
  mvn test
  ```

## Project Structure
```
health-first-server/
├── controller/
├── service/
├── entity/
├── dto/
├── util/
├── repository/
├── exception/
├── config/
└── ProviderRegistrationApplication.java
```

---

## Notes
- Passwords are hashed with bcrypt (≥12 rounds).
- Email/phone must be unique.
- Email verification is required for activation.
- Rate limiting: 5 registration attempts per IP per hour.
- All validation and error handling follows best practices. 