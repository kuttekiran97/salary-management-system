# Salary Management System

Web-based salary management for ACME's HR team. It replaces the Excel sheets used to manage pay for 10,000 employees across several countries, and answers questions about how the organisation pays people.

**User:** HR Manager

## Repository layout

| Path | Contents |
|---|---|
| `backend/` | Spring Boot REST API (Java 21, Spring Data JPA, SQLite) |
| `frontend/` | React single-page app (Vite + TypeScript) |
| `docs/requirements.md` | One-page requirements: goal, scope, what is left out and why |
| `docs/architecture.md` | System diagrams, data model, deployment and scalability |
| `docs/decisions.md` | Architecture and design decisions with trade-offs |
| `docs/ai-workflow.md` | How AI tools were used, with the prompts |

## Prerequisites

- Java 21 or later (Maven is not needed; the Maven wrapper is included)
- Node.js 20 or later

## Running locally

**Backend** (http://localhost:8081; set `PORT` to change it). The first run with the seed flag loads 10,000 employees into `backend/salary.db` (about 1–2 seconds). Later runs skip seeding because the data already exists.

```bash
cd backend
./mvnw spring-boot:run -Dspring-boot.run.arguments=--app.seed.enabled=true
```

**Frontend** (http://localhost:5173, forwards `/api` calls to the backend):

```bash
cd frontend
npm install
npm run dev
```

## API

| Method and path | Purpose |
|---|---|
| `GET /api/employees` | List employees. Query parameters: `search` (name, email or code), `country`, `department`, `jobTitle`, `sort` (`NAME`, `EMPLOYEE_CODE`, `JOINING_DATE`, `SALARY`), `descending`, `page`, `size` (max 100) |
| `GET /api/employees/{id}` | One employee |
| `POST /api/employees` | Add an employee |
| `PUT /api/employees/{id}` | Update an employee, e.g. a salary change |
| `DELETE /api/employees/{id}` | Remove an employee |
| `GET /api/reference-data` | Countries, departments and job titles for dropdowns |
| `GET /api/insights/overview` | Organisation-wide headcount and pay (USD) |
| `GET /api/insights/by-country` | Headcount and min / max / average / median / total pay per country, in local currency and USD |
| `GET /api/insights/by-job-title` | The same statistics per job title: in USD across all countries, or in local currency with `?country=IN` |

Salaries are stored in each country's local currency. Every employee response includes `annualSalaryUsd`, and sorting by `SALARY` uses the USD equivalent so different currencies compare correctly.

Errors use the standard [problem details](https://www.rfc-editor.org/rfc/rfc9457) format. Validation errors include an `errors` object that maps each field to its message:

```json
{ "status": 409, "detail": "Another employee already has this email",
  "errors": { "email": "Another employee already has this email" } }
```

## Running tests

```bash
cd backend && ./mvnw test
cd frontend && npm test
```

## Status

- **Backend:** complete. Employee management, salary insights and seed data, covered by 68 tests.
- **Frontend:** in progress.
