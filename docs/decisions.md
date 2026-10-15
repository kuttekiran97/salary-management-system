# Design Decisions

Each decision records the context, the choice and the trade-off, so it can be revisited later.

## 1. Monorepo with separate backend and frontend

- **Choice:** One repository with `backend/` and `frontend/` folders.
- **Trade-off:** Two toolchains (Maven and npm) in one repo, which is manageable at this size.

## 2. Backend: Java 21 + Spring Boot

- **Choice:** Spring Boot REST API, Java 21 (LTS).
- **Why:** Matches the Java role. Spring Boot provides validation, persistence and testing support out of the box.

## 3. Frontend: React (Vite + TypeScript)

- **Choice:** React instead of Angular.
- **Why:** The brief lists React/NextJS as the UI option. Vite keeps the build fast, and TypeScript catches API-contract mistakes early.
- **Trade-off:** The role mentions Angular. The backend API is framework-agnostic, so the UI could be replaced without backend changes.

## 4. Database: SQLite

- **Choice:** SQLite. Spring Data JPA handles employee CRUD; plain SQL (JdbcTemplate) handles statistics and bulk seeding.
- **Why:** Suggested in the brief, needs no separate server, and handles 10,000 rows easily.
- **Trade-off:** Single-writer and file-based, so it is not suitable for many concurrent users. Moving to PostgreSQL would mainly mean changing the driver and connection settings.

## 5. Flyway owns the schema; Hibernate only validates it

- **Choice:** Tables are created by versioned SQL migrations. Hibernate runs with `ddl-auto=validate`.
- **Why:** The schema is explicit, reviewable and versioned. If the Java entities and the database ever drift apart, the app refuses to start instead of failing later.
- **Trade-off:** SQLite's type names differ from what Hibernate expects (e.g. `INTEGER` vs `BIGINT`), so a few columns declare their type explicitly.

## 6. Constraints in the database as well as the service

- **Choice:** The service checks rules and returns clear messages (duplicate email, unknown country, job title outside its department). The database also enforces them with `CHECK`, `UNIQUE` and foreign keys.
- **Why:** The service gives good error messages; the database guarantees the data stays valid even if a bug, a race or a manual edit bypasses the service.

## 7. Salaries: whole numbers in local currency, compared in USD

- **Choice:** `annual_salary` is an integer in the employee's local currency. Each country has a fixed USD rate. API responses include the USD equivalent.
- **Why:** HR records pay in local currency, so stored values match their source data. Integers are exact; SQLite has no exact decimal type. Fixed rates make insights reproducible and tests deterministic.
- **Trade-off:** USD figures are approximate and do not follow market rates. Live rates are out of scope.

## 8. Sorting by salary uses the USD equivalent

- **Choice:** `sort=SALARY` orders by `annual_salary × usd_rate`.
- **Why:** Sorting raw amounts across currencies is misleading: ₹30,00,000 (about $36,000) would rank above $150,000.

## 9. Fixed job catalog

- **Choice:** Departments and job titles come from a fixed list in code, offered as dropdowns and validated by the API.
- **Why:** Free-text titles split statistics ("Sr. Engineer" vs "Senior Engineer"), which would make insights by job title unreliable.
- **Trade-off:** Adding a job title needs a code change. Managing the catalog in the UI could come later.

## 10. Statistics computed in SQL

- **Choice:** Min, max, average, median and total are calculated in one SQL query per view. SQLite has no `MEDIAN`, so the median uses window functions (`ROW_NUMBER`, `COUNT OVER`).
- **Why:** Only one row per group leaves the database, instead of 10,000 salaries. The same approach scales to much larger data.

## 11. Deterministic seed data

- **Choice:** The generator uses a fixed random seed, weighted country and role mixes, and role-based salary bands scaled per country. Rows are inserted with JDBC batches, and seeding is skipped when employees already exist.
- **Why:** Every run produces identical data, so demos, bug reports and tests are reproducible. Batching loads 10,000 rows in about 1.3 seconds.

## 12. Errors as RFC 9457 problem details

- **Choice:** All errors use the standard problem-details JSON format. Validation errors include an `errors` map from field to message. Status codes: 400 invalid input, 404 not found, 409 duplicate.
- **Why:** A standard format is predictable, and the field map lets the UI show each error next to its input.
