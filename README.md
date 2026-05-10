# Online Project Selection System

CPT202 Group 24 - Online Project Selection System is a Spring Boot web application for managing university project selection. It supports three roles: student, teacher, and administrator.

## Features

- User registration, email verification, login, logout, password reset, and profile management
- Role-based dashboards for students, teachers, and administrators
- Teacher project topic creation, editing, publishing, closing, archiving, and deletion
- Student project browsing, filtering, application submission, application tracking, and withdrawal
- Teacher approval workflow for accepting or rejecting applications
- Allocation rules including one agreed project per student and project capacity limits
- Administrator user management, category management, project archive/restore/delete, manual assignment, conflict log review, audit log review, and statistics
- Shared notifications and reusable error pages

## Technology Stack

- Java 17
- Spring Boot 3.2.5
- Spring MVC, Spring Security, Spring Data JPA
- Thymeleaf
- MySQL 8.0
- Maven

## Project Structure

```text
src/main/java/com/group24/projectselection
  config/        Security, seed data, and global error handling
  controller/    MVC and REST controllers
  model/         JPA entities
  repository/    Spring Data repositories
  service/       Business logic

src/main/resources
  templates/     Thymeleaf pages
  static/css/    Shared CSS
  application.properties.template

src/test/java    Unit, controller, security, and integration tests
test-evidence/   Sprint test evidence files
schema.sql       MySQL schema for a fresh database
seed.sql         Demo data
test-data.sql    Additional test data
```

## Prerequisites

- Java 17
- Maven 3.6+
- MySQL 8.0+

Check versions:

```bash
java -version
mvn -version
mysql --version
```

## Database Setup

Create and initialise the database:

```bash
mysql -u root -p < schema.sql
mysql -u root -p project_selection < seed.sql
```

The seed data provides demo users, categories, project topics, applications, notifications, audit logs, and conflict logs.

## Application Configuration

Create a local configuration file from the template:

```bash
cp src/main/resources/application.properties.template src/main/resources/application.properties
```

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/project_selection?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD

spring.mail.username=YOUR_SMTP_EMAIL_HERE
spring.mail.password=YOUR_SMTP_AUTH_CODE_HERE

app.base-url=http://localhost:8080
server.port=8080
```

Do not commit `src/main/resources/application.properties`. It is ignored by Git.

## Run Locally

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080/login
```

## Demo Accounts

All seeded demo accounts use the password:

```text
test123
```

| Role | Email |
| --- | --- |
| Admin | admin@xjtlu.edu.cn |
| Teacher | james.harrison@xjtlu.edu.cn |
| Student | bella.zhang@student.xjtlu.edu.cn |

The login page also contains a demo account panel that can fill these credentials automatically.

## Run Tests

Run the full test suite:

```bash
mvn test
```

Build a jar without tests:

```bash
mvn clean package -DskipTests
```

The generated jar is:

```text
target/project-selection-0.0.1-SNAPSHOT.jar
```

## Deployment Notes

For a Linux server deployment, upload the jar and provide an external configuration file such as:

```text
/opt/project-selection/application-prod.properties
```

Start the application:

```bash
cd /opt/project-selection
nohup java -jar app.jar --spring.config.location=file:/opt/project-selection/application-prod.properties > app.log 2>&1 &
```

Check the process and logs:

```bash
ps -ef | grep 'java -jar app.jar' | grep -v grep
tail -n 80 app.log
```

If port 8080 is already in use:

```bash
sudo lsof -i :8080
kill -9 <PID>
```

## Submission Packaging

Include in the source code zip:

```text
src/
test-evidence/
pom.xml
README.md
schema.sql
seed.sql
test-data.sql
.gitignore
.gitattributes
```

Do not include generated, local, or secret files:

```text
target/
*.jar
*.log
docx_*_tmp/
src/main/resources/application.properties
application-prod.properties
```

## Security Notes

- Passwords are stored as BCrypt hashes.
- CSRF protection is enabled for form and state-changing requests.
- Role-based access control is configured for student, teacher, and administrator routes.
- SMTP and database credentials must be supplied locally or on the deployment server, not committed to the repository.
