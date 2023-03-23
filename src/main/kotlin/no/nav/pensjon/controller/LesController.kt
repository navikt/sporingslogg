package no.nav.pensjon.controller

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.annotation.PostConstruct
import no.nav.pensjon.domain.LoggMelding
import no.nav.pensjon.metrics.MetricsHelper
import no.nav.pensjon.tjeneste.LoggTjeneste
import no.nav.pensjon.util.scrable
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class LesController(
    private val loggTjeneste: LoggTjeneste,
    private val tokenHelper: TokenHelper,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry())
) {

    private val log = LoggerFactory.getLogger(javaClass)

    private lateinit var tokenXlesController: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        tokenXlesController = metricsHelper.init("logg_lesX")
    }


    @GetMapping("/api/les", produces = [MediaType.APPLICATION_JSON_VALUE])
    @ProtectedWithClaims(issuer = "tokendings", claimMap = [ "acr=Level4" ])
    fun tokenXlesLoggMelding() : List<LoggMelding> {
        return tokenXlesController.measure {
            val ident = tokenHelper.getPidFromToken()

            if (ident.length != 11) throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Ugyldig ident")
            log.debug("Henter ut pid : ${ident.scrable()}")

            val result = loggTjeneste.hentAlleLoggInnslagForPerson(ident)
            log.debug("resultat size: ${result.size}")

            val loggmeldinger = result.map { logginnslag ->
                LoggMelding.fromLoggInnslag(logginnslag)
            }
            log.info("return liste for LoggMelding")
            return@measure loggmeldinger

        }
    }
}