package com.garcia.spring_boot_crash_course.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.Base64
import java.util.Date

const val TOKEN_TYPE_ACCESS = "access"
const val TOKEN_TYPE_REFRESH = "refresh"
const val ACCESS_TOKEN_VALIDITY_MS = 15 * 60 * 1000L // 15 minutes
const val REFRESH_TOKEN_VALIDITY_MS = 30 * 24 * 60 * 60 * 1000L // 30 days

@Service
class JwtService(
    @Value("\${jwt.secret}") private val jwtSecret: String // base64 encoded secret retrieved from application.properties
) {
    private val secretKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret))

    private fun generateToken(
        userId: String,
        type: String, // access or refresh
        expiry: Long
    ): String {
        val now = Date()
        val expiryDate = Date(now.time + expiry)
        return Jwts.builder()
            .subject(userId)
            .claim("type", type)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(secretKey, Jwts.SIG.HS256)
            .compact()
    }

    fun generateAccessToken(userId: String): String {
        return generateToken(userId, TOKEN_TYPE_ACCESS, ACCESS_TOKEN_VALIDITY_MS)
    }

    fun generateRefreshToken(userId: String): String {
        return generateToken(userId, TOKEN_TYPE_REFRESH, REFRESH_TOKEN_VALIDITY_MS)
    }

    fun validateAccessToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == TOKEN_TYPE_ACCESS && claims.expiration.after(Date())
    }

    fun validateRefreshToken(token: String): Boolean {
        val claims = parseAllClaims(token) ?: return false
        val tokenType = claims["type"] as? String ?: return false
        return tokenType == TOKEN_TYPE_REFRESH && claims.expiration.after(Date())
    }

    fun extractUserId(token: String): String {
        val claims = parseAllClaims(token) ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token")
        return claims.subject
    }

    private fun parseAllClaims(token: String): Claims? {
        // token -> "Bearer <token>"
        val rawToken = token.removePrefix("Bearer ").trim()
        return try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(rawToken)
                .payload
        } catch (e: Exception) {
            null
        }
    }
}