# Requirements — Salary Management System

> **Status: Final.** Incubyte left these product decisions to me ("choose what serves the HR manager best and document your reasoning"). Each choice and its reason is below.

## Goal

ACME's HR team manages salary data for 10,000 employees in several countries using Excel. This is slow and error-prone. The HR Manager needs a web application to **manage salary data** and **answer questions about how the organisation pays people**.

## User

One **HR Manager**, who keeps salary records correct and needs answers about pay across countries and roles. They sign in with a single account.

## 1. Manage employees and salaries

- Paged list of all employees, with search by name, email or employee code
- Filter by country, department and job title; sort by name, code, joining date or salary
- Add, edit and delete an employee; changing the salary is an edit
- Validation with clear messages: required fields, `EMP-00001` code format, valid and unique email and code, salary a whole number above 0, joining date not in the future, job title belonging to its department

## 2. Salary insights

There was no fixed list, so I chose the questions an HR Manager is asked most, in this order:

| # | Question | Where in the app | Why it matters |
|---|---|---|---|
| 1 | What do we pay overall? Headcount, total payroll, median and average | Insights: headline tiles | The first numbers leadership asks for; payroll is the budget |
| 2 | How does pay compare between countries? | Insights: by country (USD, plus local currency in the table) | Hiring location and cost decisions; checks local pay looks sane |
| 3 | What does each role pay, across the company? | Insights: by job title, all countries (USD) | Pay bands and offers for a role |
| 4 | What does each role pay within one country? ("Software Engineer in India") | Insights: by job title, pick a country (local currency) | Offers and raises are made in local currency against local peers |
| 5 | Who earns what, and who is highest or lowest paid? | Employees: filter and sort by salary (USD) | Spotting outliers and checking individual records |

Each question shows **min, median, average and max**. The median leads because a few high salaries pull the average up; showing both makes that skew visible.

**Considered and left for later:** pay by tenure (the joining date is stored, so this needs no data change), pay distribution histograms, and gender pay gap (needs a gender field, which also needs privacy decisions).

## 3. Employee fields

Every field is there because a feature above needs it.

| Field | Needed for |
|---|---|
| Employee code (`EMP-00001`), unique | HR's own identifier; search |
| Full name | Display; search |
| Email, unique | Search; a second identifier that HR recognises |
| Department | Filter; groups job titles |
| Job title (from a fixed list per department) | Insights by role. Free text would split statistics ("Sr. Engineer" vs "Senior Engineer") |
| Country | Insights by country; decides the currency |
| Annual salary (whole units, local currency) | All insights |
| Joining date | Sorting; ready for pay-by-tenure insights |

## 4. Salary history: current salary only

**Choice:** store the current annual salary only. Editing an employee replaces it.

**Why:**
- Every chosen insight is about how pay looks **now**; none needs past values.
- It keeps one row per employee, so lists, filters and statistics stay simple and fast.
- Adding history later is contained: a `salary_revision` table (employee, amount, effective date), with the current salary as the latest row. Existing screens keep working.

**Trade-off:** no record of raises, no "pay over time" view, and no audit trail of who changed a salary. If HR needs raise tracking or audits, history is the next feature to add.

## 5. Currency and exchange rates

**Choice:** salaries are stored in the employee's local currency, the way HR records them. Anything that mixes countries is converted to **USD at fixed rates**.

**Rates:** European Central Bank euro reference rates published on **19 October 2026** ([source](https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml)). The ECB quotes units per euro; USD per unit = 1.1225 (EUR/USD) ÷ the currency's EUR rate, rounded to 6 decimal places.

| Country | Currency | ECB rate (per EUR) | USD per unit |
|---|---|---|---|
| United States | USD | 1.1225 | 1.0 |
| Germany | EUR | 1 | 1.1225 |
| United Kingdom | GBP | 0.84205 | 1.333056 |
| Singapore | SGD | 1.4357 | 0.781849 |
| Canada | CAD | 1.5798 | 0.710533 |
| Australia | AUD | 1.6068 | 0.698593 |
| India | INR | 106.5590 | 0.010534 |

The rates and their date are stored in the `country` table (migration `V3`), returned by `/api/reference-data`, and shown on the Insights page.

**Why fixed:** the same data always gives the same answers, so reports can be compared and tests are repeatable. **Trade-off:** USD figures drift from market rates over time. To refresh, add a migration with new rates and a new as-of date.

## 6. Login

**Choice:** a single HR Manager account protects the whole API. The username and password come from environment variables (`APP_USERNAME`, `APP_PASSWORD`). If no password is set, a random one is generated and printed in the startup log, so no deployment runs with a known default.

**How it would grow:** for several HR users, replace the single account with the company's identity provider (OpenID Connect, e.g. Azure AD or Okta) through Spring Security, add roles (HR Manager edits, others view only), and record who changed each salary.

## Deliberately left out

| Item | Reason |
|---|---|
| Payroll processing, tax, payslips | A different problem; the goal is managing and understanding pay |
| Excel / CSV import | The seed script covers the starting data; import is a natural next step since HR works in Excel today |
| Live exchange rates | Fixed, dated rates keep reports repeatable (section 5) |
| Salary history | Not needed by the chosen insights (section 4) |
| Multiple users and roles | One HR Manager is the stated user (section 6) |

## Quality

- Seed script that creates 10,000 realistic employees, identical on every run
- Unit tests for validation, search, statistics, currency conversion, seeding and sign-in
- Deployed application and a demo video
