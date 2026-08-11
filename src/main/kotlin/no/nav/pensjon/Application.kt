package no.nav.pensjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
@Profile("!test")
class Application

    fun main(args: Array<String>) {
        runApplication<Application>(*args)
    }
