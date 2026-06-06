# About this project
This project manage the bank customer accounts and its transactions. 
Microservice person-client is external and independent to this one.

## Use Cases & Business Rules

- **Create account:**
    - The `account_number` is sent in the request.
    - The `client_id` field must be verified by sending a Request-Reply (RPC) message via RabbitMQ to the Client 
      microservice. If the client doesn't exist or isn't verified, the account creation must be aborted.
    - If the account type is `TIME_DEPOSIT` (ID 3), the client can optionally provide the `expiry_deposit_date` in the 
     request. If it is provided, save it. If it is not provided (null), the database trigger will automatically set it 
     to one month from the current date. For any other account types, this field must remain null. *(Note for JPA: 
     Ensure `expiry_deposit_date` is insertable, but keep `created_date`, `last_status_date`, and `last_change_date` 
      as `@Column(insertable = false, updatable = false)`)*.
    - The `balance` field is sent in the request, can start at 0.0.
    - The fields `created_date`, `last_status_date`, and `last_change_date` are strictly managed by the database. 
      Use `@Column(insertable = false, updatable = false)` in the JPA entity to prevent Hibernate from overriding them.

- **Update account:**
    - Accounts can only be updated if their `account_status_id` is different from 5 (`CLOSED`).
    - Only `account_status_id` and `expiry_deposit_date` fields are allowed to be updated.
    - The `account_status_id` cannot be set to 4 (`DORMANT`) manually, unless the `last_change_date` is older than 6 
      months compared to the current date AND the `balance` is 0.
    - The `account_status_id` cannot be set to 5 (`CLOSED`) through this use case. If attempted, return a friendly 
       response indicating that account closure must be handled by the specific "Deactivate Account" endpoint.

- **List accounts:**
    - Only list accounts with `account_status_id` different from 5 (`CLOSED`).
    - The response must be paginated, number of records can be sent in the request, if not, default to 10 records per page.
    - The list must resolve the `name` attribute from the catalog/lookup tables using the ID references 
      (e.g., return "ACTIVE" instead of just ID 2 for the status).
    - Implement optional filtering parameters for: `account_number`, `account_type`, `created_date`, `account_status`, 
      `initial_balance`, and `final_balance`.

- **Deactivate account:**
    - Only accounts with `account_status_id` different from 5 (`CLOSED`) can be deactivated.
    - The request must include the `account_number`, the refund method (`withdrawal` or `transfer` if there is a 
       remaining balance), and the target account number if `transfer` was chosen.
    - If the account `balance` > 0, the response must indicate the refunded amount. The system must register a 
      transaction of type 3 (`CASH_WITHDRAWAL`) or 4 (`TRANSFER_OUTBOUND`) with the exact amount to bring the account 
      balance to 0.
    - The account is officially deactivated by changing the `account_status_id` to 5 (`CLOSED`).

- **Register transaction:**
    - The `amount` value can be negative (transaction types 3-`CASH_WITHDRAWAL` and 4-`TRANSFER_OUTBOUND`) or 
       positive (transaction types 1-`CASH_DEPOSIT` and 2-`TRANSFER_INBOUND`).
    - The `account_id` must be validated against the database. The account must exist and its `account_status_id` 
       cannot be 5 (`CLOSED`).
    - The transaction cannot be processed if the resulting balance drops below zero. In this case, 
      return a friendly error message indicating insufficient funds.
    - Each transaction must update the `balance` field in the account table by adding/subtracting the transaction amount. 
      Ensure proper transaction isolation (ACID).

- **Account status report:**
    - The request must include: `client_identification_number`, `start_date`, and `end_date`.
    - The report must list all transactions within the time period, grouped by `account_number`, `account_type`, 
       and their respective balances.
    - The report must include the client's information (First name, last name, identification number, identification 
       type, address, email, and contact number). *Note: Since this microservice only holds `client_id`, 
       use an RPC RabbitMQ call to fetch this data from the Client microservice.*

# Project Instructions and Coding Guidelines
You are an expert programming assistant in Java 21, Spring Boot, and Hexagonal Architecture. Your goal is to help develop this microservice while maintaining a strict separation of concerns.

## 1. Technology Stack
- **Language:** Java 21 (Modern features: Records, Pattern Matching, Switch Expressions).
- **Framework:** Spring Boot 3.x
- **Database:** PostgreSQL
- **Dependency Manager:** Gradle with Kotlin DSL (`build.gradle.kts`).

## 2. Hexagonal Architecture (Strict Rules)
The project is strictly divided into three layers. **All directory, package, and file names must be written in English.** **No internal layer may depend on an external layer.**

### A. Domain Layer (`domain`)
- **Content:** Business entities, Value Objects, business exceptions, and Port interfaces.
- **GOLDEN RULE:** Zero external dependencies. It is **FORBIDDEN** to import Spring, JPA, Hibernate, Jackson, Lombok, or any infrastructure library.
- Output Ports are interfaces that define how the domain communicates with the outside world (e.g., `AccountRepository`).

### B. Application Layer (`application`)
- **Content:** Use Cases (Services) and Input Port interfaces.
- **Strict Rules:**
    - It is **FORBIDDEN** to use Spring annotations (`@Service`, `@Component`, `@Autowired`, `@Value`, etc.). The application layer must be pure Java.
    - Use Cases are standard Java classes (POJOs) that receive their dependencies (Ports) exclusively through the constructor.

### C. Infrastructure Layer (`infrastructure`)
- **Content:** Adapters (REST, JPA), controllers, Spring configurations, and output adapters.
    - **To have present**:
        - Endpoints must have API documentation (Swagger, OpenAPI).
        - Error handling must be centralized in this layer using `@ControllerAdvice`.
        - Logging must be centralized in this layer using `@Slf4j`.
- **Dependency Injection and Beans:**
    - **MANDATORY RULE:** All Use Cases from the `application` layer must be registered as Spring Beans within this infrastructure layer.
    - One or more configuration classes must be created in `infrastructure` (for example, in an `infrastructure.config.BeanConfiguration` package) annotated with `@Configuration`.
    - Inside this class, methods annotated with `@Bean` must be defined to manually instantiate the application Use Cases, passing the required port implementations (infrastructure) to them.

## 3. Code Style and Best Practices
- **Immutability:** Prefer using `record` for DTOs and Value Objects if they do not require mutability.
- **Dependency Injection:** Always prefer constructor injection. Do not use `@Autowired` on fields.
- **Error Handling:** Centralized in the infrastructure layer using `@ControllerAdvice`, translating domain exceptions into appropriate HTTP responses.
- **Naming Conventions:**
    - Output Ports: `[Name][PortType]SPI`.
    - Use Cases: `[Action][Entity]UseCase`.
    - Controllers: `[Name]Controller`

## 4. Code Generation Workflow
When I ask you to create a new feature, always proceed in this exact order or ask me before moving forward:
1. Create/Modify the model in `domain`.
2. Create the necessary Ports (Interfaces) in `domain`.
3. Implement the Use Case in `application`.
4. Implement the adapters (Controller, JPA Entity, Repository) in `infrastructure`.

## 5. Bean Configuration Example (Reference)

When you create a Use Case, remember that a configuration class similar to this one must exist in `infrastructure`:

```java
package com.tuempresa.microservicio.infrastructure.config;

import com.tuempresa.microservicio.application.usecases.CreateUserUseCase;
import com.tuempresa.microservicio.domain.ports.output.UserRepositoryPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    public CreateAccountUseCase createAccountUseCase(AccountRepositorySPI accountRepositorySPI) {
        return new CreateAccountUseCase(accountRepositorySPI);
    }
}
```
## 6. Development Environment and Containerization

The project is fully containerized and managed with Gradle (Kotlin DSL). The assistant must respect the following configurations when creating scripts, properties, or new modules:

### A. Dependency Management (Gradle Kotlin DSL)
- Any new dependency must be added to build.gradle.kts using Kotlin syntax (e.g., `implementation("org.springframework.boot:spring-boot-starter-data-jpa`")).
- It is **FORBIDDEN** to generate configuration blocks in Groovy or Maven (`pom.xml`) format.

### B. Docker and Database Architecture
The project has a multi-container environment managed by `docker-compose.yml`:
- **Database Service**: PostgreSQL (Check the environment variables in `docker-compose.yml` for credentials, database name, and port before suggesting changes to `application.yml`).
- **Application Service**: Built from the project's `Dockerfile`, which compiles the JAR using Gradle and runs it.

### C. Key Project Commands
When suggesting terminal commands, exclusively use:
- **Compile/Build:** `./gradlew clean build`
- **Spin up full environment:** `docker-compose up --build`
- **Spin up database only:** `docker-compose up devsu-account-db`

### D. Asynchronous communication
there will be an asynchronous communication with others microservices via messaging queue called `devsu-fintech-queue`, the service is defined in `docker-file.yml` with RabbitMQ.

## 7. Version Control (Git)

The project uses Git for version control. The assistant must strictly adhere to the following rules when suggesting or executing Git commands, creating branches, or writing commits.

### A. Commit Message Standard (Conventional Commits)
All commits must follow the structure: `<type>(<scope>): <short description in lowercase>`.
- **Allowed Types:**
    - `feat`: A new feature (e.g., `feat(account): add SPI output port`).
    - `fix`: A bug fix (e.g., `fix(database): fix connection url in yml`).
    - `refactor`: Code changes that neither fix a bug nor add a feature (e.g., `refactor(application): move logic to value object`).
    - `docs`: Documentation-only changes (e.g., `docs(readme): update docker guide`).
    - `chore`: Maintenance tasks, updating Gradle dependencies, etc.
- **Language:** Commit messages and branch names must be written in English

### B. Branching Strategy
- The primary branch is `main`. It is **forbidden** to suggest direct changes to it when developing a new feature.
- The working branches must follow the nomenclature: `feature/[id-tarea]-[short-description]` o `bugfix/[task-id]-[description]`.
    - *Example:* `feature/TASK-04-register-user`.