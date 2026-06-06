# Agent Status and Work Plan (Logbook)

*IInstructions for the Agent: Keep this file updated. Every time you implement a use case, a business rule, or make a 
technical decision, record it here. Mark completed tasks with [x].*

## 1. Actual project status
- **Current phase:** Design and implementation of infrastructure.
- **Estructura base:** Already created (packages `domain`, `application`, `infrastructure` configured with Gradle 
Kotlin DSL and Docker).

### 1.1 Entity descriptions
- **account**:
    - attributes:
        - account_id: primary key, autogenerate by the database. its an integer.
        - account_number: number account, this value is unique, sent in the request.
        - initial_amount: it can't be less than 0. amount with the client open the account. if isn't sent in the 
          request, the database put it in 0.
        - balance: is the balance in the account, at first time must be the same value as `initial_amount`. this one, 
          should be updated with each transaction.
        - account_status_id: foreign key integer to the table account_status. it can't be received in the request, at 
          first must be value 1 (`PENDING_ACTIVATION`).
        - client_id: representative number of foreign key from clients, its an integer. this value is received in the 
          request, but must be verified through message queue in rabbit. if not confirmed existence of a client, 
          the account can't be created. 
        - account_type_id: foreign key Integer to the account_type table. this one must be received in the request.
        - expiry_deposit_date: if the `account_type_id` is 3 (`TIME_DEPOSIT`), this date should be received in the 
          request, if not, database will fill automatically. 
        - created_date: date managed by de database.
        - last_status_date: date managed by de database.
        - last_change_date: date managed by de database.
- **transaction**:
    - attributes:
      - transaction_id: integer primary key, autogenerate by the database.
      - transaction_date: date managed by the database.
      - amount: transaction amount, received in the request; can be positive if belong to one of the 
        `transaction_type_id` 1 or 2. or negative value if belong to one of the `transaction_type_id` 3 or 4. 
      - account_number_destination: this account number must be received in the request if the `transtaction_type_id` 
         is 4, because this is the destination of the amount transaction.
      - transaction_type_id: foreign key integer to the table transaction_type, received in the request.
      - account_id: the foreign key integer to the account table, integer received in the request. 

### 1.2 Entity values, not managed in this module but are important as foreign values in main tables
- **account_type**: parameter table
    - attributes: account_type_id (primary key), name (unique), description (can be null or empty).
    - values: 1 - 'SAVINGS' - '', 2-'CHECKING'-'', 3-'TIME_DEPOSIT'-'' 
- **account_status**: parameter table
    - attributes: account_status_id (primary key), name (unique), description (can be null or empty).
    - values: 1 - 'PENDING_ACTIVATION' - 'The client has not activated the account.'
     , 2-'ACTIVE'-'', 3-'BLOCKED'-'', 4-'DORMANT'-'This account has not being used in a period of time'
     , 5-'CLOSED'-''
- **transaction_type**: parameter table
    - attributes: transaction_type_id (primary key), name (unique), description (can be null or empty).
    - values: 1 - 'CASH_DEPOSIT' - '', 2-'TRANSFER_INBOUND'-'', 3-'CASH_WITHDRAWAL'-'', 4-'TRANSFER_OUTBOUND'-''
- **account_backup**: Backup table for register account table changes. ignore this table, it will be manage by database.

## 2. Roadmap and Upcoming Use Cases
*Agent: Break down here the technical steps (ports, use cases, adapters, beans) for the requirements that I will request. 
Always strictly follow Hexagonal Architecture principles (Domain, Application, Infrastructure).*

**Tasks to be done:**

- [x] Create `hasOpenedAccounts` RabbitMQ Listener: This service must act as an RPC Consumer to check a client's 
   account status.
    - **1. DTO Creation:** Create the `RequestDTO` (containing a `client_id` number) and a `ResponseDTO` 
        (boolean result) to handle the message payload.
    - **2. Database Check:** Extract the `client_id` and verify in the database (via JPA Repository) if the client 
         has any accounts with an `account_status_id` different from `5` (`CLOSED`).
    - **3. Infrastructure Details:**
        - Pattern: RPC (Request-Reply)
        - Exchange: `accounts.exchange` (DirectExchange)
        - Request Queue (to listen): `accounts.check-request`
        - Response Queue (target for reply): `client.deactivation-response`
    - **4. Expected Behavior:** Use `@RabbitListener` on the request queue. Return the `ResponseDTO` and rely on 
           Spring Boot's automatic routing for the reply.

- [x] Implement "Create Account" Use Case:
    - Create the Domain Model, Input Port (UseCase interface), and Output Port (Repository interface).
    - Implement the logic to send an RPC message to verify `client_id` existence.
    - Create the REST Controller (Web Adapter) and JPA Adapter.

- [x] Implement "Update Account" Use Case:
    - Follow Hexagonal layers. Enforce the business rules avoiding manual state changes to DORMANT or CLOSED.

- [x] Implement "List Accounts" Use Case:
    - Build a dynamic query mechanism (e.g., JPA Specifications or QueryDSL) to handle the optional filters and pagination.
    - Map the lookup IDs to their readable String names in the Response DTO.

- [x] Implement "Deactivate Account" Use Case:
    - Implement a transactional boundary (`@Transactional`).
    - Handle the zero-balance enforcement by generating the corresponding withdrawal/transfer transaction before 
      changing the status to CLOSED.

- [ ] Implement "Register Transaction" Use Case:
    - Handle concurrency safely when updating the account balance.
    - Validate sufficient funds and account status before saving the transaction.

- [ ] Implement "Generate Account Report" Use Case:
    - **DTO Structure:**
        - *Level 1:* `client_name` (first + last), `client_identification_number`, `client_contact_number`, `client_email`.
        - *Level 2:* `account_number`, `account_type` (name), `balance`, `account_status` (name).
        - *Level 3:* `transaction_date`, `transaction_type` (name), `transaction_amount`, `transaction_description`.
  - **Integration:** Fetch the Level 1 data (Client details) by implementing a RabbitMQ RPC (Request-Reply) call to 
  the Client Microservice. The service must send a message containing the `client_identification_number` creating a 
  exchange/queue, and synchronously wait for the response payload to map the client's details into the 
  report DTO.

## 3. Technical Decisions Made
*Record here any significant changes to the code, custom exceptions, mappers, or design patterns used.*
- It is confirmed that dependency injection of the layer of `application` It will be managed centrally through a class 
`@Configuration` in the infrastructure (`BeanConfiguration`), keeping the application clean and organized.

## 4. Notes and Bugs
*Note here if business information is missing, if there is a bug in Docker, or if there are any pending dependencies.*