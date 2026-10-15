# Design Decisions

Each decision records the context, the choice and the trade-off, so it can be revisited later.

## 1. Monorepo with separate backend and frontend

- **Choice:** One repository with `backend/` and `frontend/` folders.
- **Why:** Reviewers see the whole solution and its history in one place. Each part builds and tests on its own.
- **Trade-off:** Two toolchains (Maven and npm) in one repo, which is manageable at this size.

## 2. Backend: Java 21 + Spring Boot

- **Choice:** Spring Boot REST API, Java 21 (LTS).
- **Why:** Matches the Java role. Spring Boot provides validation, persistence and testing support out of the box.

## 3. Frontend: React (Vite + TypeScript)

- **Choice:** React instead of Angular.
- **Why:** The brief lists React/NextJS as the UI option. Vite keeps the build fast, and TypeScript catches API-contract mistakes early.
- **Trade-off:** The role mentions Angular. The backend API is framework-agnostic, so the UI could be replaced without backend changes.

## 4. Database: SQLite

- **Choice:** SQLite via Spring Data JPA.
- **Why:** Suggested in the brief, needs no separate server, and handles 10,000 rows easily.
- **Trade-off:** Single-writer and file-based, so it is not suitable for many concurrent users. Moving to PostgreSQL would mainly mean changing the driver and connection settings.
