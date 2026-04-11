package com.example.demo.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ── Validation errors (400) ───────────────────────────────────────────────
    // REST API requests (Accept: application/json or path starts with /api/)
    // → return JSON 400
    // Web/Thymeleaf requests → return form view (handled in controller via BindingResult)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        List<String> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("messages", errors);
        body.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    // ── Resource not found (404) ──────────────────────────────────────────────
    // REST paths → JSON 404; web paths → HTML error view with 404 status
    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleNotFound(ResourceNotFoundException ex,
                                 HttpServletRequest request,
                                 jakarta.servlet.http.HttpServletResponse response,
                                 Model model) throws java.io.IOException {
        if (isApiRequest(request)) {
            Map<String, Object> body = Map.of(
                    "status", 404,
                    "error", "Not Found",
                    "message", ex.getMessage(),
                    "path", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
        response.setStatus(HttpStatus.NOT_FOUND.value());
        model.addAttribute("error", ex.getMessage());
        return "error";
    }

    // ── Catch-all (500) ───────────────────────────────────────────────────────
    @ExceptionHandler(Exception.class)
    public Object handleGeneral(Exception ex,
                                HttpServletRequest request,
                                jakarta.servlet.http.HttpServletResponse response,
                                Model model) {
        if (isApiRequest(request)) {
            Map<String, Object> body = Map.of(
                    "status", 500,
                    "error", "Internal Server Error",
                    "message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error",
                    "path", request.getRequestURI());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        model.addAttribute("error", "An unexpected error occurred: " + ex.getMessage());
        return "error";
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        return uri.startsWith("/api/")
                || (accept != null && accept.contains("application/json"));
    }
}
