---
name: run-e2e-tests
description: Run the full test suite and summarize results
allowed-tools: Bash(./mvnw *)
---

Run the backend tests with:
./mvnw test -Dtest=TodoControllerE2ETest

Summarize: how many passed/failed, and if any failures, show the relevant errors.
