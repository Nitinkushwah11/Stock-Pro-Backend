package com.stockpro.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Handles invalid JSON inputs (e.g., missing name, bad email format)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });

        // Wrap the map in a ResponseEntity to return a 400 Bad Request status
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    // 2. Handles the duplicate email scenario from earlier
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleUserAlreadyExists(UserAlreadyExistsException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Duplicate Entry");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", String.valueOf(HttpStatus.CONFLICT.value()));

        // Return a 409 Conflict status code
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }
    
 // 3. Handles login failures (Bad password or User not found)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeExceptions(RuntimeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        
        // If the error message is about login, return a 401 Unauthorized
        if (ex.getMessage().equals("Invalid credentials") || ex.getMessage().equals("User not found")) {
            errorResponse.put("error", "Authentication Failed");
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("status", String.valueOf(HttpStatus.UNAUTHORIZED.value()));
            return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
        }

        if (ex.getMessage().equals("Invalid or expired password reset token")
                || ex.getMessage().equals("User account is inactive")) {
            errorResponse.put("error", "Password Reset Failed");
            errorResponse.put("message", ex.getMessage());
            errorResponse.put("status", String.valueOf(HttpStatus.BAD_REQUEST.value()));
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
        
        // For any other unexpected errors, return a 500 Internal Server Error
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("status", String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
