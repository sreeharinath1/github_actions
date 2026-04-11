package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Employee;
import com.example.demo.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmployeeService Unit Tests")
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private Employee alice;
    private Employee bob;

    @BeforeEach
    void setUp() {
        alice = Employee.builder()
                .id(1L).name("Alice Johnson")
                .email("alice@example.com")
                .department("Engineering")
                .position("Developer").build();

        bob = Employee.builder()
                .id(2L).name("Bob Smith")
                .email("bob@example.com")
                .department("Marketing")
                .position("Lead").build();
    }

    // ── getAllEmployees ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllEmployees returns all employees")
    void getAllEmployees_returnsAll() {
        when(employeeRepository.findAll()).thenReturn(List.of(alice, bob));

        List<Employee> result = employeeService.getAllEmployees();

        assertThat(result).hasSize(2).contains(alice, bob);
        verify(employeeRepository).findAll();
    }

    @Test
    @DisplayName("getAllEmployees returns empty list when none exist")
    void getAllEmployees_empty() {
        when(employeeRepository.findAll()).thenReturn(List.of());

        assertThat(employeeService.getAllEmployees()).isEmpty();
    }

    // ── getEmployeeById ──────────────────────────────────────────────────────

    @Test
    @DisplayName("getEmployeeById returns employee when found")
    void getEmployeeById_found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));

        Employee result = employeeService.getEmployeeById(1L);

        assertThat(result).isEqualTo(alice);
    }

    @Test
    @DisplayName("getEmployeeById throws ResourceNotFoundException when not found")
    void getEmployeeById_notFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.getEmployeeById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ── createEmployee ───────────────────────────────────────────────────────

    @Test
    @DisplayName("createEmployee saves and returns employee")
    void createEmployee_success() {
        when(employeeRepository.existsByEmail(alice.getEmail())).thenReturn(false);
        when(employeeRepository.save(alice)).thenReturn(alice);

        Employee result = employeeService.createEmployee(alice);

        assertThat(result).isEqualTo(alice);
        verify(employeeRepository).save(alice);
    }

    @Test
    @DisplayName("createEmployee throws when email already exists")
    void createEmployee_duplicateEmail() {
        when(employeeRepository.existsByEmail(alice.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> employeeService.createEmployee(alice))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in use");

        verify(employeeRepository, never()).save(any());
    }

    // ── updateEmployee ───────────────────────────────────────────────────────

    @Test
    @DisplayName("updateEmployee updates fields and saves")
    void updateEmployee_success() {
        Employee updated = Employee.builder()
                .name("Alice Updated").email("alice2@example.com")
                .department("DevOps").position("SRE").build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.updateEmployee(1L, updated);

        assertThat(result.getName()).isEqualTo("Alice Updated");
        assertThat(result.getDepartment()).isEqualTo("DevOps");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("updateEmployee throws when employee not found")
    void updateEmployee_notFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.updateEmployee(99L, alice))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── deleteEmployee ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteEmployee deletes successfully")
    void deleteEmployee_success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(alice));

        employeeService.deleteEmployee(1L);

        verify(employeeRepository).delete(alice);
    }

    @Test
    @DisplayName("deleteEmployee throws when employee not found")
    void deleteEmployee_notFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(employeeRepository, never()).delete(any());
    }

    // ── getEmployeesByDepartment ─────────────────────────────────────────────

    @Test
    @DisplayName("getEmployeesByDepartment returns correct employees")
    void getByDepartment_success() {
        when(employeeRepository.findByDepartment("Engineering")).thenReturn(List.of(alice));

        List<Employee> result = employeeService.getEmployeesByDepartment("Engineering");

        assertThat(result).hasSize(1).contains(alice);
    }
}
