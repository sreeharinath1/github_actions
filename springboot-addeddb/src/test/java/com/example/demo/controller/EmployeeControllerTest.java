package com.example.demo.controller;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.Employee;
import com.example.demo.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController (Web) Unit Tests")
class EmployeeControllerTest {

    @Autowired MockMvc mockMvc;
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

    @Test
    @DisplayName("GET /employees renders list view with employees")
    void listEmployees_rendersView() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(alice));

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/list"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attribute("employees", hasSize(1)));
    }

    @Test
    @DisplayName("GET /employees/new renders empty form")
    void showCreateForm_rendersForm() throws Exception {
        mockMvc.perform(get("/employees/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attributeExists("employee"));
    }

    @Test
    @DisplayName("POST /employees redirects on valid input")
    void createEmployee_valid_redirects() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(alice);

        mockMvc.perform(post("/employees")
                        .param("name", "Alice Johnson")
                        .param("email", "alice@example.com")
                        .param("department", "Engineering")
                        .param("position", "Developer"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));
    }

    @Test
    @DisplayName("POST /employees stays on form when validation fails")
    void createEmployee_invalid_staysOnForm() throws Exception {
        mockMvc.perform(post("/employees")
                        .param("name", "")           // blank - should fail
                        .param("email", "not-valid") // invalid email
                        .param("department", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"));
    }

    @Test
    @DisplayName("GET /employees/edit/{id} renders form with existing data")
    void showEditForm_rendersForm() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(alice);

        mockMvc.perform(get("/employees/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/form"))
                .andExpect(model().attribute("employee", alice));
    }

    @Test
    @DisplayName("GET /employees/{id} renders detail view")
    void viewEmployee_rendersDetail() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(alice);

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("employees/detail"))
                .andExpect(model().attribute("employee", alice));
    }

    @Test
    @DisplayName("GET /employees/{id} shows error view when not found")
    void viewEmployee_notFound() throws Exception {
        when(employeeService.getEmployeeById(99L))
                .thenThrow(new ResourceNotFoundException("Employee not found with id: 99"));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error"));
    }

    @Test
    @DisplayName("GET /employees/delete/{id} redirects after deletion")
    void deleteEmployee_redirects() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(get("/employees/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));
    }
}
