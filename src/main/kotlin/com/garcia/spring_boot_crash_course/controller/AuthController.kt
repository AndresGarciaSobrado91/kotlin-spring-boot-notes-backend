package com.garcia.spring_boot_crash_course.controller

import com.garcia.spring_boot_crash_course.security.AuthService
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/auth")
class AuthController(
    private val authService: AuthService
) {
    data class AuthRequest(
        @field:Email(message = "Invalid email format")
        val email: String,
        @field:Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}\$",
            message = "Password must be at least 8 characters long and contain at least one letter and one number"
        )
        val password: String
    )

    data class RefreshRequest(
        @field:NotBlank(message = "Refresh token must not be blank")
        val refreshToken: String
    )

    @PostMapping("/register")
    fun register(@Valid @RequestBody body: AuthRequest) {
        authService.register(body.email, body.password)
    }

    @PostMapping("/login")
    fun login(@RequestBody body: AuthRequest): AuthService.TokenPair =
        authService.login(body.email, body.password)

    @PostMapping("/refresh")
    fun refreshTokens(@Valid @RequestBody body: RefreshRequest): AuthService.TokenPair =
        authService.refreshTokens(body.refreshToken)
}