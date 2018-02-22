# Testing and Continuous Integration
Gradle tasks: test and integTest are setup to run unit tests and integration tests respectively. They will fail at first test error.
These 2 tasks are suitable for running at developers machine before code checkin.<br>
Gradle tasks: unitTestReport and integTestReport are set up to run unit tests and integration tests with a summary test report instead of separate reports for sub-modules.
They are also fail-at-end. Therefore, it is suitable for CI/CDP jobs where you want to see all failed tests in one report.
# Tests require external services hosted in Docker containers
Some tests may depend on external services such as Redis, DB, Kafka etc. Docker containers are started up before test class and removed after test class even with exception.
See example 