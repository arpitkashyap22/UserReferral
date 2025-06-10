# User Signup and Referral API Specification

## Project Overview
This project is a RESTful API built with Java Spring Boot and H2 database to handle user signup with referral code tracking. A referral is considered successful only after the referred user completes their profile. The application supports signup with email/password or Google OAuth, generates unique referral codes, tracks referrals, and provides a CSV report of referral data. The application is Dockerized and prepared for AWS Elastic Beanstalk deployment (optional).

## Features
1. **User Signup Endpoint**:
   - Supports signup with email, password, name, and optional referral code.
   - Generates a unique 6-digit alphanumeric referral code for each user.
   - Validates referral code if provided; proceeds without referral if invalid or not provided.
2. **Referral Tracking**:
   - Tracks referrer and referee emails, user IDs, timestamps, and referral status (pending/completed).
   - Awards 1 referral point per successful referral (after profile completion).
3. **Profile Completion**:
   - Requires address (street, city, state, zip code), phone number, and date of birth (DOB).
   - Supports incremental profile updates via a single API endpoint.
4. **API Endpoints**:
   - Signup API
   - Profile Completion API
   - Get Referrals API (returns user’s referral code)
   - Referral Report API (includes referral details and aggregate metrics)
   - Bonus: CSV Report API (downloadable referral report)
5. **Security**:
   - Passwords encrypted with BCrypt.
   - JWT authentication for all endpoints except signup.
   - Console logging for signups, referral completions, and errors.
6. **Deployment**:
   - Dockerized application with configuration for AWS Elastic Beanstalk (optional).
   - File-based H2 database with sample users and referrals.

## Technology Stack
- **Language**: Java 17
- **Framework**: Spring Boot
- **Libraries**:
  - Spring Security (for JWT and Google OAuth)
  - Spring Data JPA (for database access)
  - Spring Web (for REST APIs)
  - BCrypt (for password encryption)
  - Apache Commons CSV (for CSV report generation)
- **Database**: H2 (file-based)
- **Testing**: JUnit 5
- **Containerization**: Docker
- **Deployment (Optional)**: AWS Elastic Beanstalk (free tier)
- **Logging**: SLF4J with console output

## Database Schema
The H2 database will have the following tables:

1. **Users**:
   - `id`: Long (Primary Key, Auto-increment)
   - `email`: String (Unique, Not Null)
   - `password`: String (BCrypt-encrypted, Not Null for email signup)
   - `name`: String (Not Null)
   - `referral_code`: String (6-digit alphanumeric, Unique, Not Null)
   - `referral_points`: Integer (Default: 0)
   - `created_at`: Timestamp (Not Null)

2. **Referrals**:
   - `id`: Long (Primary Key, Auto-increment)
   - `referrer_id`: Long (Foreign Key to Users.id)
   - `referee_id`: Long (Foreign Key to Users.id)
   - `referrer_email`: String (Not Null)
   - `referee_email`: String (Not Null)
   - `status`: String (Enum: PENDING, COMPLETED, Default: PENDING)
   - `created_at`: Timestamp (Not Null)

3. **Profiles**:
   - `user_id`: Long (Primary Key, Foreign Key to Users.id)
   - `street`: String
   - `city`: String
   - `state`: String
   - `zip_code`: String
   - `phone_number`: String
   - `dob`: Date
   - `updated_at`: Timestamp

**Initial Data**:
- 5 sample users with email/password, names, and referral codes.
- 3 sample referrals (1 completed, 2 pending).
- 2 sample profiles (1 complete, 1 partial).

## API Endpoints

### 1. Signup API
- **Endpoint**: `POST /api/v1/signup`
- **Description**: Registers a new user with email/password or Google OAuth.
- **Request Body**:
  ```json
  {
    "email": "user@example.com",
    "password": "Abcd1234",
    "name": "John Doe",
    "referralCode": "ABC123" // Optional
  }
  ```
  OR (for OAuth):
  ```json
  {
    "googleToken": "google-oauth-token",
    "name": "John Doe",
    "referralCode": "ABC123" // Optional
  }
  ```
- **Response**:
  - Success (200):
    ```json
    {
      "id": 1,
      "email": "user@example.com",
      "name": "John Doe",
      "referralCode": "XYZ789",
      "token": "jwt-token"
    }
    ```
  - Error (400):
    ```json
    {
      "error": "Invalid referral code"
    }
    ```
  - Error (409):
    ```json
    {
      "error": "Email already exists"
    }
    ```
- **Notes**:
  - Generates a 6-digit alphanumeric referral code.
  - If referral code is invalid, signup proceeds without referral.
  - Returns JWT token for authentication.

### 2. Profile Completion API
- **Endpoint**: `PATCH /api/v1/profile`
- **Description**: Updates user profile incrementally.
- **Headers**: `Authorization: Bearer <jwt-token>`
- **Request Body**:
  ```json
  {
    "street": "123 Main St",
    "city": "Springfield",
    "state": "IL",
    "zipCode": "62701",
    "phoneNumber": "123-456-7890",
    "dob": "1990-01-01"
  }
  ```
- **Response**:
  - Success (200):
    ```json
    {
      "message": "Profile updated",
      "isComplete": true
    }
    ```
  - Error (401):
    ```json
    {
      "error": "Unauthorized"
    }
    ```
- **Notes**:
  - Profile is considered complete when all fields (street, city, state, zip_code, phone_number, dob) are provided.
  - Updates referral status to COMPLETED and awards 1 referral point to referrer if applicable.

### 3. Get Referrals API
- **Endpoint**: `GET /api/v1/referrals`
- **Description**: Returns the user’s referral code.
- **Headers**: `Authorization: Bearer <jwt-token>`
- **Response**:
  - Success (200):
    ```json
    {
      "referralCode": "ABC123"
    }
    ```
  - Error (401):
    ```json
    {
      "error": "Unauthorized"
    }
    ```
- **Notes**: Only returns the current user’s referral code.

### 4. Referral Report API
- **Endpoint**: `GET /api/v1/referrals/report`
- **Description**: Returns a detailed report of referrals with aggregate metrics.
- **Headers**: `Authorization: Bearer <jwt-token>`
- **Response**:
  - Success (200):
    ```json
    {
      "referrals": [
        {
          "referrerEmail": "referrer@example.com",
          "refereeEmail": "referee@example.com",
          "referralCode": "ABC123",
          "status": "COMPLETED",
          "createdAt": "2025-06-09T10:00:00Z"
        }
      ],
      "metrics": {
        "totalReferrals": 10,
        "successfulReferrals": 5,
        "referralsPerUser": [
          {
            "email": "user@example.com",
            "totalReferrals": 3,
            "successfulReferrals": 2
          }
        ]
      }
    }
    ```
  - Error (401):
    ```json
    {
      "error": "Unauthorized"
    }
    ```

### 5. CSV Report API (Bonus)
- **Endpoint**: `GET /api/v1/referrals/report/csv`
- **Description**: Downloads a CSV report of referrals.
- **Headers**: `Authorization: Bearer <jwt-token>`
- **Response**:
  - Success (200): CSV file with `Content-Disposition: attachment; filename=referrals.csv`
    ```csv
    ReferrerEmail,RefereeEmail,ReferralCode,Status,CreatedAt
    referrer@example.com,referee@example.com,ABC123,COMPLETED,2025-06-09T10:00:00Z
    ```
  - Error (401):
    ```json
    {
      "error": "Unauthorized"
    }
    ```

## Project Structure
```
user-referral-api/
├── src/
│   ├── main/
│   │   ├── java/com/example/referral/
│   │   │   ├── config/               # Security, JWT, OAuth config
│   │   │   ├── controller/           # REST controllers
│   │   │   ├── model/               # Entity classes
│   │   │   ├── repository/          # JPA repositories
│   │   │   ├── service/             # Business logic
│   │   │   └── Application.java     # Spring Boot main class
│   │   └── resources/
│   │       ├── application.yml      # Configuration
│   │       └── data.sql            # Initial H2 data
│   └── test/                        # Unit tests
├── Dockerfile                      # Docker configuration
├── pom.xml                         # Maven dependencies
└── README.md                       # Project documentation
```

## TODOs
1. **Setup**:
   - [ ] Initialize Spring Boot project with Maven.
   - [ ] Configure H2 database (file-based) in `application.yml`.
   - [ ] Add dependencies: Spring Web, Spring Security, Spring Data JPA, H2, BCrypt, Apache Commons CSV.
   - [ ] Create `data.sql` with 5 sample users and 3 referrals.

2. **Model and Repository**:
   - [ ] Create `User`, `Referral`, and `Profile` entities with JPA annotations.
   - [ ] Implement JPA repositories for CRUD operations.

3. **Services**:
   - [ ] Implement `UserService` for signup, referral code generation, and OAuth integration.
   - [ ] Implement `ReferralService` for tracking and updating referral status.
   - [ ] Implement `ProfileService` for profile updates and completion checks.
   - [ ] Implement `ReportService` for referral reports and CSV generation.

4. **Controllers**:
   - [ ] Create `UserController` for signup and profile completion endpoints.
   - [ ] Create `ReferralController` for get referrals and report endpoints.
   - [ ] Implement input validation and error handling.

5. **Security**:
   - [ ] Configure Spring Security for JWT authentication.
   - [ ] Integrate Google OAuth for signup.
   - [ ] Encrypt passwords with BCrypt.
   - [ ] Add console logging for signups, referral completions, and errors.

6. **Testing**:
   - [ ] Write JUnit tests for controllers, services, and repositories.
   - [ ] Achieve 80%+ code coverage.
   - [ ] Test critical paths: signup, referral tracking, profile completion, CSV report.

7. **Docker**:
   - [ ] Create `Dockerfile` for Spring Boot application.
   - [ ] Configure Docker to use file-based H2 database.

8. **Documentation**:
   - [ ] Write `README.md` with setup instructions, API documentation, and dummy credentials.
   - [ ] Include curl examples for all endpoints.

9. **Optional Deployment**:
   - [ ] Prepare configuration for AWS Elastic Beanstalk (free tier).
   - [ ] Document deployment steps in README.

## Setup Instructions
1. **Prerequisites**:
   - Java 17
   - Maven
   - Docker
   - Git

2. **Clone Repository**:
   ```bash
   git clone https://github.com/<username>/user-referral-api.git
   cd user-referral-api
   ```

3. **Build and Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
   - Application runs on `http://localhost:8080`.

4. **Docker Setup**:
   ```bash
   docker build -t user-referral-api .
   docker run -p 8080:8080 user-referral-api
   ```

5. **Access H2 Console**:
   - URL: `http://localhost:8080/h2-console`
   - JDBC URL: `jdbc:h2:file:./data/referraldb`
   - Username: `sa`
   - Password: (empty)

6. **Dummy Credentials**:
   - Email: `test1@example.com`
   - Password: `Test1234`
   - Referral Code: `ABC123`

## API Documentation
### Curl Examples
1. **Signup**:
   ```bash
   curl -X POST http://localhost:8080/api/v1/signup \
   -H "Content-Type: application/json" \
   -d '{"email":"user@example.com","password":"Abcd1234","name":"John Doe","referralCode":"ABC123"}'
   ```

2. **Profile Completion**:
   ```bash
   curl -X PATCH http://localhost:8080/api/v1/profile \
   -H "Authorization: Bearer <jwt-token>" \
   -H "Content-Type: application/json" \
   -d '{"street":"123 Main St","city":"Springfield","state":"IL","zipCode":"62701","phoneNumber":"123-456-7890","dob":"1990-01-01"}'
   ```

3. **Get Referrals**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/referrals \
   -H "Authorization: Bearer <jwt-token>"
   ```

4. **Referral Report**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/referrals/report \
   -H "Authorization: Bearer <jwt-token>"
   ```

5. **CSV Report**:
   ```bash
   curl -X GET http://localhost:8080/api/v1/referrals/report/csv \
   -H "Authorization: Bearer <jwt-token>" \
   -o referrals.csv
   ```

## Testing
- **Framework**: JUnit 5
- **Coverage**: 80%+
- **Focus**: Controllers (API endpoints), Services (business logic), Repositories (database operations).
- **Run Tests**:
  ```bash
  mvn test
  ```

## Logging
- **Events Logged**:
  - User signup
  - Referral creation and completion
  - Errors (e.g., invalid referral code, unauthorized access)
- **Output**: Console via SLF4J

## Optional AWS Deployment
1. **Prerequisites**:
   - AWS account (free tier)
   - AWS CLI configured

2. **Steps**:
   - Build Docker image:
     ```bash
     docker build -t user-referral-api .
     ```
   - Push to AWS ECR (configure repository first).
   - Deploy to Elastic Beanstalk using Docker platform.
   - Update `README.md` with deployed URL.

## Notes
- The application is optimized for minimal resource usage to fit AWS free-tier limits.
- Google OAuth requires configuration of client ID and secret in `application.yml`.
- Ensure H2 database file (`referraldb.mv.db`) is persisted in a volume for Docker.