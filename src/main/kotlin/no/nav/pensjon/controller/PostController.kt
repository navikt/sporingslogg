package no.nav.pensjon.controller

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.annotation.PostConstruct
import no.nav.pensjon.controller.LoggMeldingValidator.validateRequestAsResponseRequestExcption
import no.nav.pensjon.domain.LoggInnslag
import no.nav.pensjon.domain.LoggMelding
import no.nav.pensjon.metrics.MetricsHelper
import no.nav.pensjon.tjeneste.LoggTjeneste
import no.nav.pensjon.util.scrable
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController


@RestController
class PostController(
    private val loggTjeneste: LoggTjeneste,
    private val tokenHelper: TokenHelper,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())
) {

    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var postController: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        postController = metricsHelper.init("logg_post")
    }

    @PostMapping("sporingslogg/api/post", produces = [MediaType.APPLICATION_JSON_VALUE], consumes = [MediaType.APPLICATION_JSON_VALUE])
    @ProtectedWithClaims(issuer = "servicebruker")
    fun postLoggMelding(@RequestBody request: LoggMelding) : Long {

        log.debug("Request: $request")

        return postController.measure {
            validateRequestAsResponseRequestExcption(request) //viktig må være først

            log.debug("LoggMelding Base64? = ${LoggMelding.checkForEncode(request)}")
            val loggMelding = LoggMelding.checkForAndEncode(request) //Check for base64 encode if plain text

            log.info("Følgende medling kommet inn: $loggMelding, systemBruker: ${tokenHelper.getSystemUserId()}")

            val loggId = loggTjeneste.lagreLoggInnslag(LoggInnslag.fromLoggMelding(loggMelding))
            val melding = "ID: $loggId, person: ${loggMelding.person.scrable()}, tema: ${loggMelding.tema}, mottaker: ${loggMelding.mottaker}"

            log.info("Lagret melding: $melding")

            return@measure loggId
        }
    }


}