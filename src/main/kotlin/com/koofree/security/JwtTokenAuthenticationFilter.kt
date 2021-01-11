package com.koofree.security

import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class JwtTokenAuthenticationFilter(
    private val tokenProvider: JwtTokenProvider
) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> =
        resolveToken(exchange.request).let { token: String? ->
            return if (!token.isNullOrEmpty() && tokenProvider.validateToken(token)) {
                val authentication: Authentication = tokenProvider.getAuthentication(token)

                chain
                    .filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication))
            } else {
                chain.filter(exchange)
            }
        }

    private fun resolveToken(request: ServerHttpRequest): String? {
        val bearerToken: String? = request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        return if (!bearerToken.isNullOrEmpty() && bearerToken.startsWith(HEADER_PREFIX)) {
            bearerToken.substring(7)
        } else null
    }

    companion object {
        private const val HEADER_PREFIX = "Bearer "
    }
}
