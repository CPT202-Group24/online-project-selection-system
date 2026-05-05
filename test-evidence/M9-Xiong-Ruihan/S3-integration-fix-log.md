# M9 PBI 9.3 Integration Fix Log

Date: 2026-05-05

## End-to-end scope
- register
- login
- create topic
- publish
- apply
- approve
- rule fires (auto reject + conflict log)

## Issues discovered and fixed
1. Global error flow did not consistently return HTTP status for 404/500 pages during MVC exception rendering.
- Fix: added status-mapped handlers in `GlobalExceptionHandler` for `NoResourceFoundException` (404) and fallback exception (500).

2. Access denied flow returned 403 without stable view assertion point in tests.
- Fix: wired security access denied page to `/error/403` and validated forward behavior in integration tests.

## Validation
- Added integration test class:
  - `src/test/java/com/group24/projectselection/M9EndToEndIntegrationTest.java`
- Added global error integration test class:
  - `src/test/java/com/group24/projectselection/GlobalErrorHandlingIntegrationTest.java`
