# AI Workflow

The brief asks for intentional use of AI tools. This file records how AI was used, the prompts given, and how its output was checked.

## Tools

- **Claude Code** (Anthropic) — reading the brief, planning, scaffolding, writing code and tests

## Principles

- I decide the requirements and design; AI speeds up the implementation.
- Every AI-generated change is reviewed, built and tested before it is committed.
- Tests define the behaviour, so AI output is checked against them.

## Log

### 1. Understanding the brief
- **Prompt:** "There is a doc present in the current folder, can you go through it?"
- **Outcome:** Summary of the goal, deliverables and evaluation criteria.

### 2. Clarifying questions
- **Prompt:** "What questions do we need to ask?"
- **Outcome:** AI proposed questions; I narrowed them to five that change the design (insights needed, salary history, currency, employee fields, authentication) and sent them to Incubyte.

### 3. Project setup
- **Prompt:** "Let's set up everything neatly and clearly."
- **Outcome:** Git repository, docs skeleton, Spring Boot backend and React frontend scaffolds.
- **Checked:** The first test-runner version the AI installed had known vulnerabilities (`npm audit`), so it was upgraded before committing.

### 4. Architecture
- **Prompt:** "Add the architectural diagram, how the whole system looks, and whether the system can scale."
- **Outcome:** `docs/architecture.md` with Mermaid diagrams and a staged scaling plan.
- **Corrected:** Two diagrams did not render on GitHub. Mermaid's own parser accepted them, which pointed to GitHub's renderer; the arrow labels were rewritten in a form it handles.

### 5. Schema
- **Prompt:** "Tell me the schema first."
- **Outcome:** The AI proposed two tables (`country`, `employee`). I reviewed and approved the design before any code was written.
- **Key reasoning:** Salary is stored as an integer because SQLite has no exact decimal type. The currency belongs to the country, so an employee's currency can never mismatch their country.

### 6. Backend, built test-first
- **Prompt:** "OK, let's start building."
- **Approach:** Small commits, each with its tests: schema → domain and search → service rules → REST API → insights → seed. A schema test was written first and failed before the migrations existed.
- **Issues found by tests and fixed:**
  - Spring did not recognise SQLite's constraint error code, so duplicates surfaced as generic errors. Fixed by mapping SQLite's codes (`sql-error-codes.xml`).
  - Hibernate's schema validation rejected SQLite's `INTEGER` type. Fixed by declaring the column types, not by turning validation off.
- **Product decision:** Sorting by salary across currencies would rank ₹30 lakh above $150k, so salary sorting uses the USD equivalent.
- **Verified beyond unit tests:** The app was run against a real database file. Create, update, duplicate (409) and delete (204 → 404) were checked with curl. Then 10,000 employees were seeded and every endpoint was timed; results are in `docs/architecture.md`.
