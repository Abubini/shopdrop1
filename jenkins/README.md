# Running Jenkins locally for ShopDrop

This spins up a Jenkins server in Docker that can run the project's `Jenkinsfile`, which
itself builds and tests ShopDrop inside a `maven:3.9-eclipse-temurin-17` Docker agent.

## 1. Start Jenkins

```bash
cd jenkins
docker compose up -d
```

## 2. Unlock Jenkins

Open http://localhost:8080 and paste in the initial admin password:

```bash
docker exec shopdrop-jenkins cat /var/jenkins_home/secrets/initialAdminPassword
```

## 3. Install plugins

Choose "Install suggested plugins", then also add from the plugin manager:

- **Docker Pipeline** (lets the Jenkinsfile's `agent { docker { ... } }` block work)
- **JUnit** (renders the Surefire/Failsafe test results)

## 4. Create the pipeline job

1. New Item -> Pipeline -> name it `shopdrop-ci`.
2. Under "Pipeline", choose "Pipeline script from SCM".
3. SCM: Git. Point it at this repository's URL and branch.
4. Script Path: `Jenkinsfile` (the default, already correct).
5. Save, then "Build Now".

## What the pipeline does

Stages, in order: checkout, install Chrome (needed for the Selenium system tests),
`mvn test` (unit + integration tests, with the JaCoCo coverage check), publish the
coverage report as a build artifact, `mvn verify` (Selenium system tests), then package
the application jar as a build artifact. JUnit results from both the unit/integration
stage and the Selenium stage are published so failures show up in the Jenkins UI per test.

## Demonstrating a regression (for the test summary report)

To show the pipeline catching a regression: break one of the unit tests on a branch (for
example, flip an assertion in `ShippingFeeCalculatorTest`), push it, and run the pipeline —
the "Unit & Integration Tests" stage should fail and the JUnit report should show which
test failed. Revert the change and re-run to show the pipeline going green again.
