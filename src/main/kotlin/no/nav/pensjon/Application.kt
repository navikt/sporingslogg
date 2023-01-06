package no.nav.pensjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile

@SpringBootApplication
@Profile("!unsecured-webmvctest")
class Application

    fun main(args: Array<String>) {
        runApplication<Application>(*args)
    }



