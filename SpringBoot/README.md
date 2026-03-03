# SWP391-CinemaManagementSystem-Team6

Cinema Management System - Spring Boot Application

## Project Structure

```
src/main/java/com/swp391/team6/cinema/
├── config/          # Configuration classes (DataSource, MVC, etc.)
├── controller/      # REST Controllers
├── dto/            # Data Transfer Objects
├── entity/         # JPA Entities
├── repository/     # JPA Repositories
├── service/        # Business Logic Services
├── util/           # Utility classes and helpers
└── view/           # Request/Response models
```

## Technologies

- Java 17
- Spring Boot 3.2.1
- Spring Data JPA
- MySQL Database
- Lombok
- Maven

## Setup Instructions

1. Install MySQL and create database
2. Update `application.properties` with your database credentials
3. Run the application:
   ```
   mvn spring-boot:run
   ```

## API Endpoint

Base URL: `http://localhost:8080/api`
