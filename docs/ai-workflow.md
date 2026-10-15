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
