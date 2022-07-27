package no.nav.pensjon.controller

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.pensjon.domain.LoggMelding
import no.nav.pensjon.metrics.MetricsHelper
import no.nav.pensjon.tjeneste.LoggTjeneste
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.annotation.PostConstruct


@RestController
class LesController(
    private val loggTjeneste: LoggTjeneste,
    private val tokenHelper: TokenHelper,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private lateinit var lesController: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        lesController = metricsHelper.init("logg_les")
    }

    @GetMapping("/sporingslogg/api/les", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ProtectedWithClaims(issuer = "difi", claimMap = [ "acr=Level4" ])
    fun lesLoggMelding() : List<LoggMelding> {
        return lesController.measure {
            val ident = tokenHelper.getPid()
            if (ident.length != 11) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ugyldig ident")
            log.debug("Henter ut pid : $ident")

            val result = loggTjeneste.hentAlleLoggInnslagForPerson(ident)
            log.debug("resultat size: ${result.size}")

            val loggmeldinger = result.map { logginnslag ->
                LoggMelding.fromLoggInnslag(logginnslag)
            }

            return@measure loggmeldinger
        }
    }

}