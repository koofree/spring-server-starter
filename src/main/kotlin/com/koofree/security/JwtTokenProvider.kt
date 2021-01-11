package com.koofree.security

import com.koofree.utils.toJson
import com.koofree.utils.toJsonMap
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    companion object {
        private const val AUTHORITIES_KEY = "roles"
    }

    private val secretKey: SecretKey

    init {
        val secret: String = Base64.getEncoder().encodeToString(jwtProperties.secretKey.toByteArray())
        secretKey = Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun createToken(authentication: Authentication): String {
        val username: String = authentication.name
        val authorities: Collection<GrantedAuthority> = authentication.authorities
        val claims: Claims = Jwts.claims().setSubject(username)
        claims[AUTHORITIES_KEY] = authorities.joinToString(",") { it.authority }

        val now = Date()
        val validity = Date(now.time + jwtProperties.expiredTime)
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .serializeToJsonWith { map -> map.toJson().toByteArray() }
            .compact()
    }

    fun getAuthentication(token: String): Authentication {
        val claims: Claims = Jwts.parserBuilder().setSigningKey(secretKey)
            .deserializeJsonWith { String(it).toJsonMap() }
            .build()
            .parseClaimsJws(token)
            .body
        val authorities: Collection<GrantedAuthority?> =
            AuthorityUtils.commaSeparatedStringToAuthorityList(claims[AUTHORITIES_KEY].toString())
        val principal = User(claims.subject, "", authorities)
        return UsernamePasswordAuthenticationToken(principal, token, authorities)
    }

    fun validateToken(token: String): Boolean {
        try {
            val claims: Jws<Claims> = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .deserializeJsonWith { String(it).toJsonMap() }
                .build()
                .parseClaimsJws(token)
            return !claims.body.expiration.before(Date())
        } catch (e: JwtException) {
            log.info("Invalid JWT token.", e)
            log.trace("Invalid JWT token trace.", e)
        } catch (e: IllegalArgumentException) {
            log.info("Invalid JWT token.", e)
            log.trace("Invalid JWT token trace.", e)
        }
        return false
    }

    private val log = LoggerFactory.getLogger(javaClass)
}
