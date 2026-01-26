
# SafetyNet Alerts (Backend)

Spring Boot backend implementing SafetyNet Alerts endpoints, reading/writing `runtime-data/data.json` and returning JSON per spec. Logging is enabled for every request and response; MVC architecture is followed; unit tests are provided and JaCoCo & Surefire are configured.

## How to run

```bash
mvn clean test
mvn spring-boot:run
```

## Reports
- Surefire: `target/surefire-reports/`
- JaCoCo: `target/site/jacoco/index.html`
