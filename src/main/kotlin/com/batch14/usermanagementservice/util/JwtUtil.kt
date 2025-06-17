package com.batch14.usermanagementservice.util

import com.batch14.usermanagementservice.domain.constant.Constant
import com.batch14.usermanagementservice.exception.CustomException
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtUtil {
    @Value("\${jwt.secret-key}")
    private lateinit var SECRET_KEY: String

//    private val LOG: Logger = LoggerFactory
    fun generateToken(id: Int, role: String): String {
        try {
            val signatureAlgorithm = SignatureAlgorithm.HS256
            val signingKey = Keys.hmacShaKeyFor(SECRET_KEY.toByteArray())
            val exp = Date(System.currentTimeMillis() + 900_000L) // 15 Minutes

            return Jwts.builder()
                .setSubject(id.toString())
                .claim("idUser", id.toString())
                .claim("role", role)
                .signWith(signingKey, signatureAlgorithm)
                .setExpiration(exp)
                .compact()
        } catch (e: JwtException) {
            throw CustomException("Internal Server Error", 500, Constant.STATUS_ERROR)
        }
    }

    fun decode(token: String): Claims {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY.toByteArray())
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: JwtException) {
            throw CustomException("Invalid Token", 401, Constant.STATUS_FAILED)
        }
    }


}
