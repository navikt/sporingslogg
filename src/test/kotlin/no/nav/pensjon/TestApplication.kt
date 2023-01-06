package no.nav.pensjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Profile

@SpringBootApplication
@Profile("unsecured-webmvctest")
class TestApplication : SpringBootServletInitializer() {

    fun main(args: Array<String>) {
        runApplication<TestApplication>(*args)
    }

}
