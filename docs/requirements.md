# Requirements — Salary Management System

> **Status: Draft.** Clarifying questions have been sent to Incubyte. Items marked *(pending)* will be updated when they reply.

## Goal

ACME's HR team manages salary data for 10,000 employees in several countries using Excel. This is slow and error-prone. The HR Manager needs a web application to **manage salary data** and **answer questions about how the organisation pays people**.

## User

A single **HR Manager** who maintains employee salary records and needs answers about pay across countries and roles.

## In scope

### 1. Manage employees and salaries
- Paginated employee list (10,000 rows) with search by name or employee ID
- Filter by country and job title; sort by name or salary
- Add, edit and delete an employee and their salary
- Validation: required fields, positive salary, valid country and currency

### 2. Salary insights
- By country: headcount and min / max / average / median salary
- By job title, optionally within a country (e.g. "Software Engineer in India vs. USA")
- Total payroll per country and overall

### 3. Data and quality
- Seed script that creates 10,000 realistic employees
- Unit tests for the core logic (validation, salary statistics, currency conversion)
- Deployed application with a demo video

## Deliberately left out

| Item | Reason |
|---|---|
| Payroll processing, tax, payslips | A different problem; the goal is managing and understanding pay |
| Excel / CSV import | The seed script covers initial data; import can be added later |
| Live exchange rates | Fixed rates are enough for comparison and keep tests deterministic |
| Login and roles *(pending)* | Single-user tool for now |
| Salary history *(pending)* | Current salary only, unless history is required |

## Open questions (sent to Incubyte)

1. Which questions about pay should the HR Manager be able to answer?
2. Salary history, or current salary only?
3. Should cross-country comparisons use one currency, with fixed rates?
4. Which fields should an employee record include?
5. Is authentication or role-based access expected?
