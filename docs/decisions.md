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

- **Choice:** `annual_salary` is an integer in the employee's local currency. Each country has a fixed USD rate taken from the European Central Bank reference rates of 19 October 2026, stored with that date (`rate_as_of`). API responses include the USD equivalent, and the Insights page names the source and date.
- **Why:** HR records pay in local currency, so stored values match their source data. Integers are exact; SQLite has no exact decimal type. Fixed rates make insights reproducible and tests deterministic. A published source and date make the USD figures checkable.
- **Trade-off:** USD figures drift from market rates over time. Refreshing means a new migration with new rates and date. Live rates are out of scope, since they would make the same report change from day to day.

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

## 13. Frontend: MUI with server-side paging

- **Choice:** MUI components, the MUI X Data Grid in server mode, and MUI X Charts.
- **Why:** The grid asks the API for one page at a time, so the browser never holds all 10,000 employees. Filtering and sorting happen in the database, consistent with the backend design. MUI is widely used and well documented.
- **Trade-off:** The data grid is large (about 150 kB gzipped). Pages are code-split, so it loads only on the Employees page, and the charts only on Insights.

## 14. Validation in the form as well as the API

- **Choice:** The form checks format rules (required fields, code format, email, positive whole salary, no future date) before sending. Rules that need data, such as duplicate emails, are checked only by the API, and its field errors are shown next to the matching input.
- **Why:** Instant feedback for simple mistakes, with the API remaining the single source of truth.

## 15. Insights as charts and tables

- **Choice:** Median salary per country and per job title as single-colour horizontal bars, sorted highest first, with values at the bar ends. Every chart has a table view with min, median, average and max.
- **Why:** Horizontal bars keep long job titles readable. The median is shown rather than the average because a few high salaries pull the average up. The table gives exact figures and works with screen readers.
- **Checked:** The bar colour passes a contrast check against both the light and dark backgrounds.

## 16. Current salary only, no history

- **Choice:** each employee has one current salary; an edit replaces it.
- **Why:** every insight chosen (requirements, section 2) is about pay now. One row per employee keeps lists and statistics simple and fast.
- **Trade-off:** no raise history and no audit trail. Adding a `salary_revision` table later does not change the existing screens.

## 17. Sign-in: one account, stateless HTTP Basic

- **Choice:** Spring Security protects `/api/**`. Every request carries HTTP Basic credentials, and the server keeps no session. The account comes from `APP_USERNAME` / `APP_PASSWORD`; with no password set, a random one is generated and logged. The UI keeps the credentials in `sessionStorage` and returns to its sign-in page on any 401.
- **Why:** with no cookies there is nothing for CSRF to exploit, the API stays stateless (any instance can serve any request), and the frontend can be hosted on another domain with only a CORS setting. 401 responses omit `WWW-Authenticate`, so browsers show the app's page rather than their own pop-up. Generating a password prevents a deployment from running with a known default.
- **Trade-off:** the password is sent with every request (safe only over HTTPS) and sits in `sessionStorage` until the tab closes, so a cross-site scripting bug could read it. For more users, the next step is the company's identity provider (OpenID Connect) with short-lived tokens, roles, and an audit log.
