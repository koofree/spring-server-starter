package com.koofree.security

import org.springframework.beans.factory.ObjectProvider
import org.springframework.boot.autoconfigure.web.WebProperties
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.ErrorAttributes
import org.springframework.context.ApplicationContext
import org.springframework.core.annotation.Order
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.json
import org.springframework.web.reactive.result.view.ViewResolver

@Component
@Order(-2)
@Suppress("LeakingThis")
class GlobalErrorWebExceptionHandler(
    errorAttributes: ErrorAttributes,
    resources: WebProperties.Resources,
    applicationContext: ApplicationContext,
    viewResolvers: ObjectProvider<ViewResolver>,
    serverCodecConfigurer: ServerCodecConfigurer
) : AbstractErrorWebExceptionHandler(errorAttributes, resources, applicationContext) {

    init {
        setViewResolvers(viewResolvers.toList())
        setMessageWriters(serverCodecConfigurer.writers)
        setMessageReaders(serverCodecConfigurer.readers)
    }

    override fun getRoutingFunction(errorAttributes: ErrorAttributes): RouterFunction<ServerResponse> =
        RouterFunctions.route(RequestPredicates.all()) { request ->
            getErrorAttributes(
                request,
                ErrorAttributeOptions.of(
                    ErrorAttributeOptions.Include.EXCEPTION,
                    ErrorAttributeOptions.Include.BINDING_ERRORS,
                    ErrorAttributeOptions.Include.MESSAGE
                )
            )
                .let {
                    ServerResponse
                        .status(it.getValue("status") as Int)
                        .json()
                        .body(BodyInserters.fromValue(it))
                }
        }
}
