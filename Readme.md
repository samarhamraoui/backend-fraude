# Fraud Management Backend

This is the backend service for the Fraud Management microservice. It is built using **Spring Boot** and provides APIs for fraud detection, management, and reporting. The backend interacts with a PostgreSQL database and is secured using JWT-based authentication.

---

## Features

- **Authentication**: JWT-based authentication for secure API access.
- **Role-Based Access Control**: Different roles and permissions for users.
- **Fraud Detection**: APIs for detecting and managing fraud cases.
- **Swagger UI**: Interactive API documentation.
- **Email Notifications**: Integrated email service for user notifications.
- **CORS Configuration**: Explicit origin handling with support for frontend integration.

---

## Prerequisites

- **Java 17** or higher
- **Maven 3.8+**
- **PostgreSQL** (distant or local database)
- **Docker** (optional, for containerized deployments)

---

## Getting Started

### 1. Clone the Repository

```bash
git clone https://github.com/your-repo/backend-fraude.git
cd backend-fraude
```

### 2. Configure Environment Variables

Create an `.env` file in the root directory and configure the following variables:

```env
# Database Configuration
DB_HOST=127.0.0.1
DB_PORT=5432
DB_NAME=example_database
DB_USER=example_user
DB_PASSWORD=example_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=86400000

# Logging Configuration
LOGGING_FILE_PATH=/app/logs/backend.log
LOGGING_LEVEL=INFO

# Mail Configuration
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=example_email@example.com
MAIL_PASSWORD=example_email_password
MAIL_FROM=example_email@example.com

# Frontend Link
FRONTEND_LINK=http://localhost:4200
```

### 3. Build and Run Locally

1. Build the project:
   ```bash
   mvn clean package
   ```

2. Run the application:
   ```bash
   java -jar target/backend-fraude.war
   ```

By default, the application will run on `http://localhost:8080`.

---

## Docker Deployment

### 1. Build the Docker Image

```bash
docker build -t backend-fraude .
```

### 2. Run the Docker Container

```bash
docker run --env-file .env -p 8080:8080 backend-fraude
```

### 3. Using Docker Compose

A `docker-compose.yml` file is provided for easier deployment.

```bash
docker-compose up --build
```

---

## API Documentation

The API documentation is available at [Swagger UI](http://localhost:8080/swagger-ui/index.html).

---

## Project Structure

```
src/
├── main/
│   ├── java/com/example/backend/
│   │   ├── conf/         # Configuration files (CORS, Security, Swagger)
│   │   ├── controllers/  # REST API Controllers
│   │   ├── dao/          # Data Access Objects
│   │   ├── services/     # Business Logic
│   │   └── utils/        # Utility Classes
│   ├── resources/
│   │   ├── application.properties  # Default Configurations
│   │   └── log4j2.xml              # Logging Configuration
├── test/                          # Unit and Integration Tests
```

---

## Key Endpoints

| Method | Endpoint                      | Description                    |
|--------|-------------------------------|--------------------------------|
| GET    | `/v3/api-docs`                | OpenAPI Documentation          |
| GET    | `/swagger-ui/index.html`      | Swagger UI                     |
| POST   | `/login`                      | User Login                     |
| POST   | `/forgot-password`            | Forgot Password                |
| POST   | `/reset-password`             | Reset Password                 |

---

## Security

- **JWT Authentication**: All endpoints (except public ones) are secured using JWT tokens.
- **CORS Configuration**: The backend explicitly allows origins and methods for secure frontend integration.

---

## Logging

Logs are written to the file specified in the `LOGGING_FILE_PATH` environment variable. By default, logs are stored in `/app/logs/backend.log`.

---

## Troubleshooting

1. **CORS Issues**: Ensure `allowedOriginPatterns` in `GlobalCorsConfiguration` includes your frontend URL.
2. **Database Connection**: Verify that the PostgreSQL database credentials in `.env` are correct.
3. **Swagger Not Accessible**: Ensure the application is running on `http://localhost:8080`.

---

## Contributing

1. Fork the repository.
2. Create a feature branch: `git checkout -b feature-name`.
3. Commit your changes: `git commit -m 'Add feature'`.
4. Push to the branch: `git push origin feature-name`.
5. Open a pull request.

---

## License

This project is licensed under the [Apache License 2.0](LICENSE).

---
