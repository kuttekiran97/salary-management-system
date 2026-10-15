# Architecture

> Diagrams use Mermaid, which GitHub renders automatically. The data model is provisional until the clarifying questions are answered.

## 1. System overview

The HR Manager uses a React single-page app in the browser. It calls a Spring Boot REST API, which stores data in SQLite.

```mermaid
flowchart LR
    HR["HR Manager<br/>browser"]

    subgraph FE["Frontend: React SPA"]
        UI["Pages<br/>Employees and Insights"]
        API_CLIENT["Typed API client"]
    end

    subgraph BE["Backend: Spring Boot"]
        CTRL["REST controllers<br/>employees and insights endpoints"]
        SVC["Services<br/>validation, statistics,<br/>currency conversion"]
        REPO["Repositories<br/>Spring Data JPA"]
    end

    DB[("SQLite<br/>schema managed by Flyway")]
    SEED["Seed script<br/>10,000 employees"]

    HR --> UI --> API_CLIENT
    API_CLIENT -->|"HTTPS + JSON"| CTRL
    CTRL --> SVC --> REPO --> DB
    SEED --> DB
```

| Part | Responsibility |
|---|---|
| **React SPA** | Screens, forms, client-side validation feedback. No business rules. |
| **Controllers** | HTTP only: parse requests, return status codes and JSON. |
| **Services** | Business rules: validation, salary statistics, currency conversion. Unit-tested without a database. |
| **Repositories** | Database access. Filtering, sorting, paging and aggregation run in SQL. |
| **SQLite + Flyway** | Storage. Versioned migrations create and change the schema. |

## 2. Backend structure

Code is grouped by feature, not by technical layer, so each feature can be read and changed in one place.

```
com.acme.salary
├── employee/     Employee entity, repository, service, controller, DTOs
├── insights/     Salary statistics queries and endpoints
├── currency/     Fixed exchange rates and conversion
└── common/       Error handling, shared validation
```

## 3. Request flow: "Average salary by country"

```mermaid
sequenceDiagram
    participant UI as React SPA
    participant C as InsightsController
    participant S as InsightsService
    participant R as Repository
    participant DB as SQLite

    UI->>C: GET /api/insights/by-country
    C->>S: salaryStatsByCountry()
    S->>R: aggregate grouped by country
    R->>DB: SELECT country, COUNT, MIN, MAX, AVG ... GROUP BY country
    DB-->>R: one row per country
    R-->>S: results
    S->>S: convert to reporting currency, compute median
    S-->>C: stats
    C-->>UI: 200 OK (JSON)
```

Aggregation runs in the database, so only a few rows per country are sent back to the application, not all 10,000 employees.

## 4. Data model (provisional)

```mermaid
erDiagram
    EMPLOYEE {
        long id PK
        string employee_code UK
        string full_name
        string job_title
        string country_code FK
        decimal salary "annual, local currency"
        date joining_date
    }
    COUNTRY {
        string code PK "ISO 3166, e.g. IN"
        string name
        string currency_code "ISO 4217, e.g. INR"
        decimal rate_to_usd "fixed rate"
    }
    COUNTRY ||--o{ EMPLOYEE : employs
```

- Salaries are stored in the **local currency**, the way HR records them, and converted only for cross-country comparisons.
- Money uses `decimal` (`BigDecimal` in Java), never floating point, to avoid rounding errors.
- Indexes on `country_code`, `job_title` and `full_name` support the filters and search.
- If salary history is required, a `SALARY_REVISION` table (employee, amount, effective date) would be added.

## 5. Deployment

```mermaid
flowchart LR
    B["Browser"] -->|"static files"| STATIC["Static host<br/>React build"]
    B -->|"API calls over HTTPS"| APP["Container<br/>Spring Boot JAR"]
    APP --> VOL[("Persistent volume<br/>salary.db")]
```

The frontend is a static build that can be served from any static host or CDN. The backend runs as a single container, with the SQLite file on a persistent volume.

## 6. Scalability

### Current target: 10,000 employees, one HR user

The current design handles this comfortably:

- **Pagination:** the API returns one page (e.g. 50 rows) at a time, never all 10,000.
- **Database-side filtering and aggregation:** the database does the work, and only results cross the network.
- **Indexes** on the filtered and searched columns keep queries fast as data grows.
- **Small data:** 10,000 rows is a few MB in SQLite, and aggregate queries over it are expected to take milliseconds.

### Limits of this design

| Limit | Why |
|---|---|
| One writer at a time | SQLite locks the whole database for each write. Fine for one HR user, not for many concurrent editors. |
| One backend instance | The SQLite file lives on a single machine, so the backend can't be run as several instances. |
| Statistics computed on every request | Fine at 10,000 rows; slower at millions. |

### How it would scale

The layered design means each step below changes one part without rewriting the others.

| Stage | Scale | Change |
|---|---|---|
| **1. Now** | 10k employees, 1 HR user | SQLite, single instance |
| **2. Growth** | 100k–1M employees, a whole HR team | Move to **PostgreSQL** (a driver and config change, since JPA and Flyway hide the database), add **caching** for insights |
| **3. Large** | Millions of employees, many concurrent users | **Several stateless backend instances** behind a load balancer, **read replicas** for reporting, **precomputed summary tables** refreshed when salaries change |

The backend keeps no user state in memory between requests, so stage 3 only needs more instances; no code changes are required to support that.

```mermaid
flowchart LR
    B["Browsers"] --> CDN["CDN<br/>React build"]
    B --> LB["Load balancer"]
    LB --> A1["API instance 1"]
    LB --> A2["API instance 2"]
    LB --> A3["API instance N"]
    A1 & A2 & A3 --> CACHE[("Cache<br/>insights")]
    A1 & A2 & A3 -->|"writes"| PG[("PostgreSQL<br/>primary")]
    A1 & A2 & A3 -->|"reports"| RR[("Read replica")]
    PG -.->|"replication"| RR
```

At that scale, **authentication and role-based access** would also be needed, because several HR users would see and change sensitive salary data.
