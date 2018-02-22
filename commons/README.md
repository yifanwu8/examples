# Fail at end test & integration tests with summary report
[Gradle tasks](https://github.com/yifanwu8/examples/blob/master/build.gradle): test and integTest are setup to run unit tests and integration tests respectively. They will fail at first test error.
These 2 tasks are suitable for running at developers machine before code checkin.<br>
[Gradle tasks](https://github.com/yifanwu8/examples/blob/master/build.gradle): unitTestReport and integTestReport are set up to run unit tests and integration tests with a summary test report instead of separate reports for sub-modules.
They are also fail-at-end. Therefore, it is suitable for CI/CDP jobs where you want to see all failed tests in one report.
# Tests require external services hosted in Docker containers
Some tests may depend on external services such as Redis, DB, Kafka etc. Docker containers are started up before test class and removed after test class even with exception.
See example <https://github.com/yifanwu8/examples/blob/master/commons/src/test/java/com/yifanwu/examples/testing/AmqDockerIT.java>

### How to run:
To run integration tests:
```bash
./gradlew :integTest
```
To run integration test with summary report:
```bash
./gradlew :integTestReport
```
Report is at ./build/reports/allIntegTests/index.html