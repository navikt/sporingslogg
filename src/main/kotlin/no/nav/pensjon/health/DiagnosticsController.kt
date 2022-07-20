package no.nav.pensjon.health

import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Unprotected
class DiagnosticsController {

    @GetMapping("/ping")
    fun ping() = ResponseEntity.ok().build<Unit>()

    @GetMapping("/internal/isalive")
    fun isalive() =ResponseEntity.ok("Is alive")

    @GetMapping("/internal/isready")
    fun isready() = ResponseEntity.ok("Is ready")

}

