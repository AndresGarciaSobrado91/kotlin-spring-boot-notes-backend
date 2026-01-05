package com.garcia.spring_boot_crash_course.security

import com.garcia.spring_boot_crash_course.database.model.RefreshToken
import com.garcia.spring_boot_crash_course.database.model.User
import com.garcia.spring_boot_crash_course.database.repository.RefreshTokenRepository
import com.garcia.spring_boot_crash_course.database.repository.UserRepository
import org.bson.types.ObjectId
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException
import java.security.MessageDigest
import java.time.Instant
import java.util.Base64

@Service
class AuthService(
    private val jwtService: JwtService,
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val passwordHashEncoder: PasswordHashEncoder
) {
    data class TokenPair(
        val accessToken: String,
        val refreshToken: String
    )

    fun register(email: String, password: String): User {
        val hashedPassword = passwordHashEncoder.encode(password)
        val user = userRepository.findByEmail(email)
        if (user != null) {
            throw ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered")
        }
        return userRepository.save(
            User(
                email = email,
                hashedPassword = hashedPassword
            )
        )
    }

    fun login(email: String, password: String): TokenPair {
        val user = userRepository.findByEmail(email)
            ?: throw BadCredentialsException("Invalid email or password")

        if (!passwordHashEncoder.matches(password, user.hashedPassword)) {
            throw BadCredentialsException("Invalid email or password")
        }

        refreshTokenRepository.deleteAllByUserId(user.id)

        val accessToken = jwtService.generateAccessToken(user.id.toHexString())
        val refreshToken = jwtService.generateRefreshToken(user.id.toHexString())

        storeRefreshToken(user.id, refreshToken)

        return TokenPair(accessToken, refreshToken)
    }

    @Transactional
    fun refreshTokens(refreshToken: String): TokenPair {
        if (!jwtService.validateRefreshToken(refreshToken)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
        }

        val userId = jwtService.extractUserId(refreshToken)
        val user = userRepository.findById(ObjectId(userId)).orElseThrow {
            ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")
        }

        val hashed = hashToken(refreshToken)
        val currentRefreshToken = refreshTokenRepository.findByUserIdAndHashedToken(user.id, hashed)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token")

        refreshTokenRepository.deleteByUserIdAndHashedToken(user.id, currentRefreshToken.hashedToken)

        val accessToken = jwtService.generateAccessToken(userId)
        val newRefreshToken = jwtService.generateRefreshToken(userId)

        storeRefreshToken(
            userId = user.id,
            rawRefreshToken = newRefreshToken,
            expiresAt = currentRefreshToken.expiresAt
        )
        return TokenPair(accessToken, newRefreshToken)
    }

    private fun storeRefreshToken(userId: ObjectId, rawRefreshToken: String, expiresAt: Instant? = null) {
        val hashed = hashToken(rawRefreshToken)
        val expiresAt = expiresAt ?: Instant.now().plusMillis(REFRESH_TOKEN_VALIDITY_MS)

        refreshTokenRepository.save(
            RefreshToken(
                userId = userId,
                hashedToken = hashed,
                expiresAt = expiresAt
            )
        )
    }

    // Passwords are hashed using bcrypt, but tokens can be hashed simply with SHA-256 for storage
    private fun hashToken(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(rawToken.toByteArray())
        return Base64.getEncoder().encodeToString(hashBytes)
    }
}