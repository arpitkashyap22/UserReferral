# User Referral System

A Spring Boot application that implements a user referral system with JWT authentication, profile management, and referral tracking.

## Features

- User authentication with JWT
- Profile management with completion tracking
- Referral system with status tracking
- Referral reports and analytics
- CSV export functionality
- Asynchronous referral processing
- Role-based access control

## Technical Stack

- Java 17
- Spring Boot 3.2.3
- Spring Security with JWT
- Spring Data JPA
- H2 Database (can be easily switched to other databases)
- Maven for dependency management
- Lombok for reducing boilerplate code

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Git

## Setup and Installation

1. Clone the repository:
```bash
git clone https://github.com/arpitkashyap22/UserReferral.git
cd user-referral
```

2. Configure the application:
   - The application uses H2 database by default
   - JWT secret is configured in `application.properties`
   - Default admin user is created on startup

3. Build the application:
```bash
./mvnw clean install
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## API Documentation

### Authentication Endpoints

- POST `/api/users/signup` - Register a new user
- POST `/api/users/login` - Login and get JWT token

### Profile Endpoints

- GET `/api/profiles` - Get user's profile
- PUT `/api/profiles` - Update user's profile

### Referral Endpoints

- POST `/api/referrals` - Create a new referral
- GET `/api/referrals` - Get user's referrals
- PUT `/api/referrals/{id}/complete` - Complete a referral

### Report Endpoints

- GET `/api/reports/referrals` - Get referral report
- GET `/api/reports/referrals/csv` - Download referral report as CSV

## Implementation Approach

1. **Authentication & Security**
   - Implemented JWT-based authentication
   - Role-based access control (ADMIN, USER)
   - Secure password storage with BCrypt
   - Token validation and refresh mechanism

2. **Profile Management**
   - Profile completion tracking
   - Automatic referral processing on profile completion
   - Asynchronous processing to handle referral updates

3. **Referral System**
   - Bidirectional referral tracking
   - Status management (PENDING, COMPLETED)
   - Authorization checks for referral actions
   - Automatic completion when profiles are ready

4. **Reporting**
   - Comprehensive referral metrics
   - CSV export functionality
   - Per-user referral statistics
   - Success rate calculations

## Deployment Notes

1. **Database Configuration**
   - Currently using H2 database for development
   - For production, update `application.properties` with your database credentials
   - Consider using a production-grade database like PostgreSQL

2. **Security Considerations**
   - Update JWT secret in production
   - Configure proper CORS settings
   - Use HTTPS in production
   - Consider rate limiting for API endpoints

3. **Performance Optimization**
   - Asynchronous processing for referral updates
   - Efficient database queries with proper indexing
   - Caching for frequently accessed data

## Known Issues and Limitations

1. **Current Limitations**
   - No email verification system
   - No password reset functionality
   - Limited to H2 database in current setup
   - No rate limiting implementation

2. **Future Improvements**
   - Add email verification
   - Implement password reset
   - Add more comprehensive reporting
   - Implement rate limiting
   - Add unit and integration tests
   - Add API documentation with Swagger/OpenAPI

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For any queries or support, please open an issue in the GitHub repository. 
