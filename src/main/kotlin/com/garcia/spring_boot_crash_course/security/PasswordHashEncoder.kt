package com.garcia.spring_boot_crash_course.security

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class PasswordHashEncoder {

    private val bCrypt = BCryptPasswordEncoder()

    fun encode(rawPassword: String): String {
        return bCrypt.encode(rawPassword)
    }

    fun matches(rawPassword: String, hashedPassword: String): Boolean {
        return bCrypt.matches(rawPassword, hashedPassword)
    }
}