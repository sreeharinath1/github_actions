package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Employee;
import com.example.demo.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeRestController.class)
@DisplayName("EmployeeRestController Unit Tests")
class EmployeeRestControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean EmployeeService employeeService;

    private Employee alice;

    @BeforeEach
    void setUp() {
        alice = Employee.builder()
                .id(1L).name("Alice Johnson")
                .email("alice@example.com")
                .department("Engineering")
                .position("Developer").build();
    }

    // ── GET /api/employees ────────────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/employees returns 200 with list")
    void getAll_returns200() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(alice));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Alice Johnson")))
                .andExpect(jsonPath("$[0].email", is("alice@example.com")));
    }

    @Test
    @DisplayName("GET /api/employees returns empty array when none exist")
    void getAll_empty() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ── GET /api/employees/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("GET /api/employees/{id} returns 200 when found")
    void getById_found() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(alice);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.department", is("Engineering")));
    }

    @Test
    @DisplayName("GET /api/employees/{id} returns 404 when not found")
    void getById_notFound() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    // ── POST /api/employees ───────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/employees returns 201 on success")
    void create_returns201() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(alice);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alice)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Alice Johnson")));
    }

    @Test
    @DisplayName("POST /api/employees returns 400 when name is blank")
    void create_invalidName() throws Exception {
        Employee invalid = Employee.builder()
                .name("").email("x@example.com").department("IT").build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/employees returns 400 when email is invalid")
    void create_invalidEmail() throws Exception {
        Employee invalid = Employee.builder()
                .name("John").email("not-an-email").department("IT").build();

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    // ── PUT /api/employees/{id} ───────────────────────────────────────────────

    @Test
    @DisplayName("PUT /api/employees/{id} returns 200 on success")
    void update_returns200() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(alice);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(alice)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Alice Johnson")));
    }

    // ── DELETE /api/employees/{id} ────────────────────────────────────────────

    @Test
    @DisplayName("DELETE /api/employees/{id} returns 204 on success")
    void delete_returns204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} returns 404 when not found")
    void delete_notFound() throws Exception {
        doThrow(new ResourceNotFoundException("Not found"))
                .when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    // ── GET /api/employees/department/{dept} ──────────────────────────────────

    @Test
    @DisplayName("GET /api/employees/department/{dept} filters by department")
    void getByDepartment() throws Exception {
        when(employeeService.getEmployeesByDepartment("Engineering")).thenReturn(List.of(alice));

        mockMvc.perform(get("/api/employees/department/Engineering"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].department", is("Engineering")));
    }
}
