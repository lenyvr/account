# About this project
This project manage the bank customer accounts and its transactions. 
Microservice person-client is external and independent to this one.

## Use cases
- Create account:
    - the number account is sent in the request
    - if an account is the type `TIME_DEPOSIT` (which id is 3), the field `expiry_deposit_date` should be filled, 
      otherwise the bd will set a period time of one month by default.
    - The `client_id` field, must be verified asynchronously sending a message to a queue where microservice client can 
      read and answer, if isn't verified or client doesn't exist, the account can't be created.
    - the `balance` field can be 0.0
    - the fields `created_date`, `last_status_date` and `last_change_date` are managed for the database. 
- Update account:
    - Only can be updated accounts with `account_status_id` different from value 5 (`CLOSED`).
    - Only can be updated the fields `account_status_id` and `expiry_deposit_date`.
    - `account_status_id` field can't be set to status id 4 (`DORMANT`), unless the `last_change_date` value field be 
    older than 6 months comparing with the current date and `balance` value field be in 0.
    - `account_status_id` field can't be set to status id 5 (`CLOSED`), send a response indicting that the status can't 
    be set in that service and that for that purpose there is another endpoint.
- List accounts:
    - Only can be listed accounts with `account_status_id` different from value 5 (`CLOSED`).
    - The list should be paginated.
    - The list must get the `name` attribute from the parametrized table, by the id reference in main table account, 
    for e.g., `ACTIVE` value for accounts with account_status_id value 2. 
    - It must have an optionals filters for each of the followings fields: `account_number`, `account_type`, 
     `created_date`, `account_status`, `initial_balance` and `final_balance`.
- Deactivate account:
    - Only can be deactivated accounts with `account_status_id` different from value 5 (`CLOSED`).
    - The request must include the `account_number` to be deactivated, the refund method (withdrawal or transfer, 
      if there is a balance) and the account number to which the refund amount will be transferred if the 'transfer' 
       option was chosen.
    - If the account has in `balance` field a value > 0, a message in the response must indicate the amount that will 
     be refund to the client (with the option chosen in the request) and a transaction type id 3 
    (`CASH_WITHDRAWAL`)  or type id 4 (`TRANSFER_OUTBOUND`) must be registered with the amount that let in 0 the balance 
      account
    - An account is inactivated changing `account_status_id` field to status id 5 (`CLOSED`).
- Register transaction: 
    - The `amount` value can be negative (transaction types 3-`CASH_WITHDRAWAL` and  4-`TRANSFER_OUTBOUND`) or positive
      (transactions type 1-`CASH_DEPOSIT` and 2-`TRANSFER_INBOUND`).
    - The `account_id` value must be verified toward the account table records, must exist the account with that id 
      and the account encountered can't be in account_status_id 5 (`CLOSED`).
    - The transaction can't be done if the amount value of the transaction is add to the balance value in the account 
      and the result is less than zero. response with a friendly message indicating that the balance isn't enough.
    - Each transaction must update the `balance` field by adding the transaction amount to the current account balance in 
     account table with tha corresponding `account_id` from the transaction.
- Account status report:
    - In the request must be the client `identification_number`, `start_date`and `end_date`.
    - this report must list all transactions in that period of time, group by the number_account, account_type and
     account balance that belong.
    - the report must show client information such as: first and last name, identification_number and identification_type
    address, email and contact number.

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