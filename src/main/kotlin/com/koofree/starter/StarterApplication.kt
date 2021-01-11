package com.koofree.starter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
internal class StarterApplication

internal fun main(args: Array<String>) {
    runApplication<StarterApplication>(*args)
}
