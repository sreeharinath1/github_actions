package com.example.demo.integration;

import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Repository Integration Tests using @DataJpaTest (in-memory H2).
 * These are NOT full-stack – only the JPA layer is loaded.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("EmployeeRepository Integration Tests")
class EmployeeRepositoryIT {

    @Autowired
    EmployeeRepository employeeRepository;

    private Employee alice;
    private Employee bob;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        alice = employeeRepository.save(Employee.builder()
                .name("Alice").email("alice@test.com")
                .department("Engineering").position("Dev").build());
        bob = employeeRepository.save(Employee.builder()
                .name("Bob").email("bob@test.com")
                .department("Marketing").position("Lead").build());
    }

    @Test
    @DisplayName("findAll returns all saved employees")
    void findAll_returnsAll() {
        List<Employee> all = employeeRepository.findAll();
        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("findById returns correct employee")
    void findById_found() {
        Optional<Employee> found = employeeRepository.findById(alice.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findById returns empty when not found")
    void findById_notFound() {
        Optional<Employee> found = employeeRepository.findById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByEmail returns correct employee")
    void findByEmail_found() {
        Optional<Employee> found = employeeRepository.findByEmail("alice@test.com");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("findByEmail returns empty for unknown email")
    void findByEmail_notFound() {
        Optional<Employee> found = employeeRepository.findByEmail("nobody@test.com");
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findByDepartment returns only matching employees")
    void findByDepartment() {
        List<Employee> engineers = employeeRepository.findByDepartment("Engineering");
        assertThat(engineers).hasSize(1);
        assertThat(engineers.get(0).getName()).isEqualTo("Alice");
    }

    @Test
    @DisplayName("existsByEmail returns true when email exists")
    void existsByEmail_true() {
        assertThat(employeeRepository.existsByEmail("bob@test.com")).isTrue();
    }

    @Test
    @DisplayName("existsByEmail returns false when email absent")
    void existsByEmail_false() {
        assertThat(employeeRepository.existsByEmail("nobody@test.com")).isFalse();
    }

    @Test
    @DisplayName("save persists new employee with generated ID")
    void save_persistsEmployee() {
        Employee carol = Employee.builder()
                .name("Carol").email("carol@test.com")
                .department("HR").position("Manager").build();

        Employee saved = employeeRepository.save(carol);

        assertThat(saved.getId()).isNotNull();
        assertThat(employeeRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("delete removes employee from DB")
    void delete_removesEmployee() {
        employeeRepository.delete(alice);

        assertThat(employeeRepository.findById(alice.getId())).isEmpty();
        assertThat(employeeRepository.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("update changes employee fields")
    void update_changesFields() {
        alice.setDepartment("DevOps");
        alice.setPosition("SRE");
        employeeRepository.save(alice);

        Employee updated = employeeRepository.findById(alice.getId()).orElseThrow();
        assertThat(updated.getDepartment()).isEqualTo("DevOps");
        assertThat(updated.getPosition()).isEqualTo("SRE");
    }
}
