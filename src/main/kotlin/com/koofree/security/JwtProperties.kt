package com.koofree.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.UUID

@ConfigurationProperties(prefix = "jwt")
@Component
class JwtProperties {
    var secretKey: String = UUID.randomUUID().toString()
    var expiredTime: Long = 60 * 60 * 1000
}
