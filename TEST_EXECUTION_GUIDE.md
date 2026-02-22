# Test Execution Guide

## Running E2E Tests

### Basic Commands

```bash
# Run all E2E tests
./mvnw test -Dtest=TodoControllerE2ETest

# Run and skip other tests
./mvnw test -Dtest=TodoControllerE2ETest -DskipOtherTests=true

# Run specific nested test class
./mvnw test -Dtest=TodoControllerE2ETest\$GetTodosTests
./mvnw test -Dtest=TodoControllerE2ETest\$CreateTodoTests
./mvnw test -Dtest=TodoControllerE2ETest\$UpdateTodoTests
./mvnw test -Dtest=TodoControllerE2ETest\$DeleteTodosTests

# Run with verbose output
./mvnw test -Dtest=TodoControllerE2ETest -X

# Run with detailed error messages
./mvnw test -Dtest=TodoControllerE2ETest -e

# Run and generate HTML report
./mvnw test -Dtest=TodoControllerE2ETest
# Report will be in: target/site/surefire-report.html
```

## Expected Output

When tests pass, you should see output similar to:

```
[INFO] 
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 8.234 s -- in com.example.todowebapp.api.TodoControllerE2ETest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] BUILD SUCCESS
```

## Test Output Breakdown

Each test execution will show:

```
[INFO] Running com.example.todowebapp.api.TodoControllerE2ETest
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0 -- GetTodosTests
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- CreateTodoTests
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- UpdateTodoTests
[INFO] Tests run: 6, Failures: 0, Errors: 0, Skipped: 0 -- DeleteTodosTests
[INFO] 
[INFO] Total: 22 tests, 0 failures, 0 errors
```

## Troubleshooting Test Failures

### Issue: Connection to database failed

**Symptom**: `Failed to load ApplicationContext`

**Solution**:
1. Check `schema.sql` is in `src/test/resources`
2. Verify `application-test.properties` exists
3. Check H2 dependency in pom.xml:
   ```xml
   <dependency>
       <groupId>com.h2database</groupId>
       <artifactId>h2</artifactId>
       <scope>runtime</scope>
   </dependency>
   ```

### Issue: Authentication fails

**Symptom**: `shouldReturn401WhenAuthorizationHeaderMissing` passes but login fails

**Solution**:
1. Verify `/api/login` endpoint exists and works
2. Check JWT secret in `application-test.properties` matches main config
3. Verify PasswordEncoder bean is available

### Issue: Tests timeout

**Symptom**: `Tests hang or take very long`

**Solution**:
1. Check database connectivity
2. Verify connection pool settings in `application-test.properties`
3. Increase timeout: `./mvnw test -DtestFailureIgnore=false -DargLine="-Dtimeout=120"`

### Issue: "Cannot find symbol" errors

**Symptom**: Compilation errors on imports

**Solution**:
1. Run `./mvnw clean`
2. Ensure all dependencies are in pom.xml
3. Reload IDE project structure

## Analyzing Test Results

### View test report
```bash
open target/site/surefire-report.html  # macOS
xdg-open target/site/surefire-report.html  # Linux
start target\site\surefire-report.html  # Windows
```

### View detailed test logs
```bash
cat target/surefire-reports/com.example.todowebapp.api.TodoControllerE2ETest.txt
```

### Run single test method
```bash
./mvnw test -Dtest=TodoControllerE2ETest#shouldCreateNewTodoSuccessfully
```

## Performance Tips

### Speed up test execution

```bash
# Run tests in parallel (if available)
./mvnw test -DtestFailureIgnore=false -DrunOrder=random

# Skip certain tests
./mvnw test -Dtest=TodoControllerE2ETest -DexcludedGroups=slow

# Use offline mode (if dependencies cached)
./mvnw test -o
```

### Check test execution time

```bash
./mvnw test -Dtest=TodoControllerE2ETest -DtestFailureIgnore=false 2>&1 | grep "elapsed"
```

## Integrating with IDE

### IntelliJ IDEA
1. Right-click `TodoControllerE2ETest` class
2. Select "Run 'TodoControllerE2ETest'"
3. Or click the green play button next to class name

### VS Code
1. Install "Test Runner for Java" extension
2. Click "Run Test" link above class
3. Or use command palette: `Java: Test Explorer`

### Eclipse
1. Right-click test file
2. Select "Run As" > "JUnit Test"
3. Or use "JUnit" view

## Continuous Integration

### GitHub Actions Example

```yaml
name: E2E Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run E2E Tests
        run: ./mvnw test -Dtest=TodoControllerE2ETest
      - name: Upload Test Results
        if: always()
        uses: actions/upload-artifact@v2
        with:
          name: test-results
          path: target/surefire-reports/
```

### GitLab CI Example

```yaml
test:e2e:
  stage: test
  image: maven:3.8.1-openjdk-17
  script:
    - ./mvnw test -Dtest=TodoControllerE2ETest
  artifacts:
    reports:
      junit: target/surefire-reports/TEST-*.xml
```

## Test Data Reference

### Test User
- **Email**: `testuser@example.com`
- **Password**: `testpass123`
- **Role**: `ROLE_BASIC_USER`

### Sample Todo Data
Tests create temporary todos with:
- Descriptions: e.g., "First task", "Second task"
- Due dates: Various dates (past, present, future)
- Completion status: Both complete and incomplete
- Completion dates: When applicable

### API Endpoints Tested
- `GET /api/todos` - Retrieve all user's todos
- `POST /api/todo/create` - Create new todo
- `PUT /api/todo/update` - Update existing todo
- `DELETE /api/todo/delete` - Delete one or multiple todos

## Test Isolation

Each test:
1. ✅ Starts with clean database
2. ✅ Creates own test data
3. ✅ Verifies results
4. ✅ Cleans up automatically
5. ✅ Doesn't affect other tests

## Success Criteria

A successful test run shows:
```
✅ All 22 tests pass
✅ No compilation errors
✅ No timeout errors
✅ Database initialized properly
✅ All assertions pass
✅ All cleanup executed
```

## Getting Help

If tests fail:

1. **Check logs**: `./mvnw test -Dtest=TodoControllerE2ETest -X 2>&1 | tee test.log`
2. **Read error messages**: Look for assertion failures or exceptions
3. **Verify setup**: Check database initialization in schema.sql
4. **Debug single test**: Run one test at a time
5. **Check dependencies**: Verify all imports in TodoControllerE2ETest.java

## Next Steps After Tests Pass

1. ✅ Add to CI/CD pipeline
2. ✅ Increase test coverage with additional scenarios
3. ✅ Monitor test execution time
4. ✅ Add performance benchmarks
5. ✅ Document any custom test configurations

---

**Happy Testing!** 🚀

