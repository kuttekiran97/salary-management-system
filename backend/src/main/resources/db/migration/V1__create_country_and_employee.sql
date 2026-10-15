-- Countries ACME employs people in. Each country has one currency.
CREATE TABLE country (
    code          TEXT    PRIMARY KEY,                       -- ISO 3166-1 alpha-2, e.g. IN
    name          TEXT    NOT NULL,
    currency_code TEXT    NOT NULL,                          -- ISO 4217, e.g. INR
    usd_rate      NUMERIC NOT NULL CHECK (usd_rate > 0)      -- USD value of one unit of local currency
);

-- Salaries are whole units of the employee's local currency.
-- Dates are ISO-8601 text: SQLite has no date type, and ISO text sorts correctly.
CREATE TABLE employee (
    id            INTEGER PRIMARY KEY AUTOINCREMENT,
    employee_code TEXT    NOT NULL UNIQUE,
    full_name     TEXT    NOT NULL,
    email         TEXT    NOT NULL UNIQUE,
    job_title     TEXT    NOT NULL,
    department    TEXT    NOT NULL,
    country_code  TEXT    NOT NULL REFERENCES country (code),
    annual_salary INTEGER NOT NULL CHECK (annual_salary > 0),
    joining_date  TEXT    NOT NULL,                          -- yyyy-MM-dd
    created_at    TEXT    NOT NULL,                          -- ISO-8601 instant
    updated_at    TEXT    NOT NULL
);

-- Most insights filter by country, or country and job title
CREATE INDEX idx_employee_country_title ON employee (country_code, job_title);
-- Job-title insights across all countries
CREATE INDEX idx_employee_job_title ON employee (job_title);
