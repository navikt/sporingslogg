package no.nav.pensjon.metrics

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import no.nav.pensjon.tjeneste.LoggRepository
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * Publiserer antall loggInnslag per tema som gauges. Verdiene hentes av en schedulert jobb og caches,
 * slik at Prometheus-scrape aldri kjører SQL. Bruk max by (tema) i Grafana når det er flere pods.
 */
@Configuration
@EnableScheduling
@Profile("!test")
class StatistikkMetrikker(
    private val repository: LoggRepository,
    private val registry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val verdier = ConcurrentHashMap<Pair<String, String>, AtomicLong>()
    private val sisteOppdatering = AtomicLong(0)

    init {
        Gauge.builder("sporingslogg_statistikk_sist_oppdatert_sekunder", sisteOppdatering) { it.get().toDouble() }
            .register(registry)
    }

    @Scheduled(cron = "\${sporingslogg.statistikk.cron:0 30 3 * * *}")
    fun oppdater() {
        try {
            oppdaterFra(repository.tellPerTema())
        } catch (e: Exception) {
            logger.error("Klarte ikke å oppdatere statistikk-metrikker, beholder forrige verdi", e)
        }
    }

    internal fun oppdaterFra(rader: List<Array<Any>>) {
        val ferske = rader.associate { it[0] as String to Pair((it[1] as Number).toLong(), (it[2] as Number).toLong()) }
        ferske.forEach { (tema, antall) ->
            sett(tema, "alle", antall.first)
            sett(tema, "med_samtykke", antall.second)
            sett(tema, "uten_samtykke", antall.first - antall.second)
        }
        // temaer som er borte fra DB nullstilles i stedet for å vise gammel verdi
        verdier.keys.filter { it.first !in ferske }.forEach { verdier[it]?.set(0) }
        sisteOppdatering.set(System.currentTimeMillis() / 1000)
    }

    private fun sett(tema: String, samtykke: String, verdi: Long) {
        verdier.computeIfAbsent(tema to samtykke) { n ->
            AtomicLong().also {
                Gauge.builder("sporingslogg_loggInnslag_antall", it) { a -> a.get().toDouble() }
                    .tag("tema", n.first).tag("samtykke", n.second).register(registry)
            }
        }.set(verdi)
    }
}
