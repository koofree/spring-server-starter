package com.koofree.starter.interfaces

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

@RestController
@RequestMapping("/hello")
@PreAuthorize("hasRole('ROLE_USER')")
internal class HelloController {
    @GetMapping
    fun get(): Mono<String> {
        return "hello".toMono()
    }
}
