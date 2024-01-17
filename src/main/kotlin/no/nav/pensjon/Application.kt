package no.nav.pensjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
//@Profile("!test")
class Application

    fun main(args: Array<String>) {
        runApplication<Application>(*args)
    }



