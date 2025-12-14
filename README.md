# Library Management System

## Overview
The Library Management System is a comprehensive application designed to manage library operations efficiently. It provides features for managing books, users, borrow logs, and notifications, making it an ideal solution for libraries of all sizes.

## Features
- **User Management**: Registration, login, password reset, and user profile management.
- **Book Management**: Add, edit, view, and delete books.
- **Borrow Logs**: Track borrowed books and their return status.
- **Notifications**: Notify users about due dates, new books, and other updates.
- **Dashboard**: A centralized view for administrators and users.

## Technologies Used
- **Backend**: Java, Spring Boot
- **Frontend**: HTML, CSS, Thymeleaf templates
- **Database**: MySql
- **Build Tool**: Maven
- **Testing**: JUnit, Mockito

## Project Structure
```
Library-Management-System/
├── HELP.md
├── mvnw
├── mvnw.cmd
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── oops/
│   │   ├── resources/
│   │       ├── application.properties
│   │       ├── documentation/
│   │       │   └── concepts.md
│   │       └── templates/
│   └── test/
│       └── java/
│           └── com/
├── target/
│   ├── classes/
│   ├── maven-status/
│   └── surefire-reports/
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.8.9 or higher
- Git

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Nishit-2212/Library-Management-System.git
   ```
2. Navigate to the project directory:
   ```bash
   cd Library-Management-System
   ```
3. Build the project:
   ```bash
   ./mvnw clean install
   ```
4. Run the application:
   ```bash
   ./mvnw spring-boot:run
   ```

### Accessing the Application
- Open your browser and navigate to `http://localhost:8080`.

## Configuration
- The application properties can be configured in `src/main/resources/application.properties`.
- Default database: H2 (in-memory). For production, configure a persistent database like MySQL or PostgreSQL.

## Testing
Run the test cases using the following command:
```bash
./mvnw test
```
Test reports are generated in the `target/surefire-reports` directory.

## Contributing
Contributions are welcome! Follow these steps:
1. Fork the repository.
2. Create a new branch:
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. Commit your changes:
   ```bash
   git commit -m "Add your message here"
   ```
4. Push to the branch:
   ```bash
   git push origin feature/your-feature-name
   ```
5. Open a pull request.

## License
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

## Acknowledgments
- Spring Boot documentation
- Thymeleaf documentation
- Open-source libraries and tools used in this project

---

Feel free to reach out for any queries or suggestions!
