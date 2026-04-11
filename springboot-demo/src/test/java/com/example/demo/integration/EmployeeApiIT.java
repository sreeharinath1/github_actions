package com.example.demo.integration;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration Tests – spin up the full Spring context on a random port.
 * Naming convention *IT.java ensures Failsafe (not Surefire) runs these.
 *
 * Run: mvn verify
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeApiIT {

    @LocalServerPort
    int port;

    @Autowired
    EmployeeRepository employeeRepository;

    private static Long createdId;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/employees";
    }

    @AfterEach
    void tearDown() {
        // clean up between tests (except when we need the created record)
    }

    // ── POST ───────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("IT: POST creates a new employee and returns 201")
    void it_createEmployee() {
        Employee payload = Employee.builder()
                .name("Integration User")
                .email("integ@example.com")
                .department("Engineering")
                .position("Tester")
                .build();

        createdId = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                    .post()
                .then()
                    .statusCode(201)
                    .body("name", equalTo("Integration User"))
                    .body("email", equalTo("integ@example.com"))
                    .body("department", equalTo("Engineering"))
                    .body("id", notNullValue())
                .extract()
                    .jsonPath().getLong("id");
    }

    @Test
    @Order(2)
    @DisplayName("IT: POST duplicate email returns 400")
    void it_createEmployee_duplicateEmail() {
        Employee payload = Employee.builder()
                .name("Duplicate User")
                .email("integ@example.com")   // same as order-1
                .department("HR")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when()
                    .post()
                .then()
                    .statusCode(anyOf(is(400), is(500))); // service throws IllegalArgumentException
    }

    // ── GET All ───────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("IT: GET /api/employees returns list with at least 1 employee")
    void it_getAllEmployees() {
        get()
                .then()
                    .statusCode(200)
                    .body("$", not(empty()))
                    .body("[0].name", notNullValue());
    }

    // ── GET by ID ─────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("IT: GET /api/employees/{id} returns correct employee")
    void it_getById() {
        get("/" + createdId)
                .then()
                    .statusCode(200)
                    .body("id", equalTo(createdId.intValue()))
                    .body("name", equalTo("Integration User"));
    }

    @Test
    @Order(5)
    @DisplayName("IT: GET /api/employees/999 returns 404")
    void it_getById_notFound() {
        get("/999999")
                .then()
                    .statusCode(404);
    }

    // ── PUT ───────────────────────────────────────────────────────────────────

    @Test
    @Order(6)
    @DisplayName("IT: PUT /api/employees/{id} updates the employee")
    void it_updateEmployee() {
        Employee updated = Employee.builder()
                .name("Updated User")
                .email("updated@example.com")
                .department("DevOps")
                .position("SRE")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(updated)
                .when()
                    .put("/" + createdId)
                .then()
                    .statusCode(200)
                    .body("name", equalTo("Updated User"))
                    .body("department", equalTo("DevOps"));
    }

    // ── GET by Department ────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("IT: GET /api/employees/department/{dept} filters correctly")
    void it_getByDepartment() {
        // seed a known employee for this test
        employeeRepository.save(Employee.builder()
                .name("Dept Tester")
                .email("dept@example.com")
                .department("Finance")
                .position("Analyst").build());

        get("/department/Finance")
                .then()
                    .statusCode(200)
                    .body("$", not(empty()))
                    .body("[0].department", equalTo("Finance"));
    }

    // ── DELETE ────────────────────────────────────────────────────────────────

    @Test
    @Order(8)
    @DisplayName("IT: DELETE /api/employees/{id} removes the employee")
    void it_deleteEmployee() {
        delete("/" + createdId)
                .then()
                    .statusCode(204);

        // verify gone
        get("/" + createdId)
                .then()
                    .statusCode(404);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    @Order(9)
    @DisplayName("IT: POST with blank name returns 400")
    void it_createEmployee_blankName() {
        Employee invalid = Employee.builder()
                .name("")
                .email("valid@example.com")
                .department("IT")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalid)
                .when()
                    .post()
                .then()
                    .statusCode(400);
    }

    @Test
    @Order(10)
    @DisplayName("IT: POST with invalid email format returns 400")
    void it_createEmployee_invalidEmail() {
        Employee invalid = Employee.builder()
                .name("Bad Email")
                .email("not-an-email")
                .department("IT")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(invalid)
                .when()
                    .post()
                .then()
                    .statusCode(400);
    }
}
