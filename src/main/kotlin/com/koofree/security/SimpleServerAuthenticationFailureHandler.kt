package com.koofree.security

import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationFailureHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Component
class SimpleServerAuthenticationFailureHandler : ServerAuthenticationFailureHandler {
    override fun onAuthenticationFailure(
        webFilterExchange: WebFilterExchange,
        exception: AuthenticationException
    ): Mono<Void> = webFilterExchange
        .exchange
        .response
        .writeWith { sink ->
            sink.onError(ResponseStatusException(HttpStatus.UNAUTHORIZED, "login failed"))
            sink.onComplete()
        }
}
