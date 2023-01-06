package no.nav.pensjon.tjeneste

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.annotation.PostConstruct
import jakarta.transaction.Transactional
import no.nav.pensjon.domain.LoggInnslag
import no.nav.pensjon.metrics.MetricsHelper
import no.nav.pensjon.tjeneste.ValideringTjeneste.validerIkkeBlank
import no.nav.pensjon.tjeneste.ValideringTjeneste.validerMaxLengde
import no.nav.pensjon.util.scrable
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LoggTjeneste(
    private val loggRepository: LoggRepository,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry()) ) {

    private val log = LoggerFactory.getLogger(javaClass)

    private lateinit var lagreLoggInnslag: MetricsHelper.Metric
    private lateinit var hentLoggInnslag: MetricsHelper.Metric
    private lateinit var finnLoggInnslag: MetricsHelper.Metric
    private lateinit var validerOgLagre: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        lagreLoggInnslag = metricsHelper.init("lagre")
        hentLoggInnslag = metricsHelper.init("hent")
        finnLoggInnslag = metricsHelper.init("finn")
        validerOgLagre = metricsHelper.init("validerOgLagre")
        log.info("LoggTjeneste klar..")
    }

    fun lagreLoggInnslag(loggInnslag: LoggInnslag): Long {
        log.debug("Lagrer for person " + loggInnslag.person.scrable() + ", mottaker: " + loggInnslag.mottaker + ", tema: " + loggInnslag.tema)

        return validerOgLagre.measure {

        validerIkkeBlank(loggInnslag.person, "person")
        validerMaxLengde(loggInnslag.person, 11, "person")
        validerIkkeBlank(loggInnslag.mottaker, "mottaker")
        validerMaxLengde(loggInnslag.mottaker, 9, "mottaker")
        validerIkkeBlank(loggInnslag.tema, "tema")
        validerMaxLengde(loggInnslag.tema, 3, "tema")
        validerIkkeBlank(loggInnslag.hjemmel, "hjemmel")
        validerMaxLengde(loggInnslag.hjemmel, 100, "hjemmel")
        validerIkkeBlank(loggInnslag.leverteData, "data")
        validerMaxLengde(loggInnslag.leverteData, 1000000, "data")
        validerMaxLengde(loggInnslag.samtykkeToken, 1000, "samtykketoken")
        validerMaxLengde(loggInnslag.foresporsel, 100000, "forespørsel")
        validerMaxLengde(loggInnslag.leverandor, 9, "leverandør")
        if (loggInnslag.uthentingsTidspunkt == null) {
            loggInnslag.uthentingsTidspunkt = LocalDateTime.now()
        }
        return@measure saveLoggInnslag(loggInnslag)
        }
    }

    @Transactional
    fun saveLoggInnslag(loggInnslag: LoggInnslag): Long {
        return lagreLoggInnslag.measure {
            loggRepository.saveAndFlush(loggInnslag)
            val id: Long? = loggInnslag.id
            return@measure id!!
        }
   }

    @Transactional
    fun hentAlleLoggInnslagForPerson(person: String): List<LoggInnslag> {
        return hentLoggInnslag.measure {
            return@measure loggRepository.hantAlleLoggInnslagForPerson(person)
        }
    }

    @Transactional
    fun finnAllePersonerStarterMed(ident: String): List<String> {
        return finnLoggInnslag.measure {
            return@measure loggRepository.finnAllePersonStarterMed(ident).mapNotNull { innslag -> innslag.person }
        }
    }

    @Transactional
    fun countAlleLoggInnslagForPerson(ident: String): Int = loggRepository.countAlleLoggInnslagForPerson(ident)

}