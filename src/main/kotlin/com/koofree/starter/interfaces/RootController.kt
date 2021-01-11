package com.koofree.starter.interfaces

import org.springframework.boot.info.BuildProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
internal class RootController(private val buildProperties: BuildProperties) {

    @GetMapping("/")
    fun get(): Mono<BuildProperties> {
        return buildProperties.toMono()
    }
}
