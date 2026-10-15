# Salary Management System

Web-based salary management for ACME's HR team. It replaces the Excel sheets used to manage pay for 10,000 employees across several countries, and answers questions about how the organisation pays people.

**User:** HR Manager

## Repository layout

| Path | Contents |
|---|---|
| `backend/` | Spring Boot REST API (Java 21, Spring Data JPA, SQLite) |
| `frontend/` | React single-page app (Vite + TypeScript) |
| `docs/requirements.md` | One-page requirements: goal, scope, what is left out and why |
| `docs/decisions.md` | Architecture and design decisions with trade-offs |
| `docs/ai-workflow.md` | How AI tools were used, with the prompts |

## Prerequisites

- Java 21 or later (Maven is not needed; the Maven wrapper is included)
- Node.js 20 or later

## Running locally

**Backend** (http://localhost:8080):

```bash
cd backend
./mvnw spring-boot:run
```

**Frontend** (http://localhost:5173, forwards `/api` calls to the backend):

```bash
cd frontend
npm install
npm run dev
```

## Running tests

```bash
cd backend && ./mvnw test
cd frontend && npm test
```

## Status

Project setup is complete. Features are being built incrementally; see the commit history.
