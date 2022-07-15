package no.nav.pensjon

import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer
import org.springframework.context.annotation.Profile

@SpringBootApplication
@EnableJwtTokenValidation(ignore = ["org.springframework", "no.nav.pensjon"])
@Profile("unsecured-webmvctest")
class UnsecuredWebMvcTestLauncher : SpringBootServletInitializer() {

    fun main(args: Array<String>) {
        runApplication<UnsecuredWebMvcTestLauncher>(*args)
    }

}
