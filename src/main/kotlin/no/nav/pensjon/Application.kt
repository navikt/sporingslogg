package no.nav.pensjon

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile

@EnableJwtTokenValidation
@SpringBootApplication
@Profile("!unsecured-webmvctest")
class Application

    fun main(args: Array<String>) {
        runApplication<Application>(*args)
    }



