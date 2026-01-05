package com.garcia.spring_boot_crash_course

import jakarta.validation.ConstraintViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

// Note: Remember to use @Valid in your controller methods to trigger validation
@RestControllerAdvice
class GlobalExceptionHandler {
    // You can define global exception handling methods here

    // Handle field validation errors like @NotBlank, @Email, etc (in @RequestBody)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, Any>> {
        val errors = ex.bindingResult.fieldErrors.map {
            mapOf(
                "field" to it.field,
                "message" to (it.defaultMessage ?: "Invalid value")
            )
        }

        return ResponseEntity
            .badRequest()
            .body(mapOf("errors" to errors))
    }

    // Handle validation errors for method parameters like @RequestParam, @PathVariable, etc.
    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(ex: ConstraintViolationException): ResponseEntity<Map<String, Any>> {
        val errors = ex.constraintViolations.map {
            it.message
        }

        return ResponseEntity
            .badRequest()
            .body(mapOf("errors" to errors))
    }
}