# Agent Status and Work Plan (Logbook)

*IInstructions for the Agent: Keep this file updated. Every time you implement a use case, a business rule, or make a 
technical decision, record it here. Mark completed tasks with [x].*

## 1. Actual project status
- **Current phase:** Design and implementation of infrastructure.
- **Estructura base:** Already created (packages `domain`, `application`, `infrastructure` configured with Gradle 
Kotlin DSL and Docker).

### 1.1 Entity descriptions
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

## 2. Roadmap and Upcoming Use Cases
*Agent: Break down here the technical steps (ports, use cases, adapters, beans) for the requirements that I will request.*
**Tasks to be done:**
- [ ]


## 3. Technical Decisions Made
*Record here any significant changes to the code, custom exceptions, mappers, or design patterns used.*
- It is confirmed that dependency injection of the layer of `application` It will be managed centrally through a class 
`@Configuration` in the infrastructure (`BeanConfiguration`), keeping the application clean and organized.

## 4. Notes and Bugs
*Note here if business information is missing, if there is a bug in Docker, or if there are any pending dependencies.*