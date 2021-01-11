package com.koofree.security

import com.koofree.utils.toJson
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.WebFilterExchange
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class SimpleServerAuthenticationSuccessHandler(
    private val jwtTokenProvider: JwtTokenProvider
) : ServerAuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        webFilterExchange: WebFilterExchange,
        authentication: Authentication
    ): Mono<Void> = webFilterExchange
        .exchange
        .response
        .writeWith { sink ->
            sink.onNext(
                DefaultDataBufferFactory()
                    .wrap(
                        AuthenticationSuccessDto(jwtTokenProvider.createToken(authentication))
                            .toJson()
                            .toByteArray()
                    )
            )
            sink.onComplete()
        }
}
