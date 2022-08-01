package no.nav.pensjon.listener

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import no.nav.pensjon.controller.LoggMeldingValidator.validateRequest
import no.nav.pensjon.controller.SporingsloggValidationException
import no.nav.pensjon.domain.LoggInnslag
import no.nav.pensjon.domain.LoggMelding
import no.nav.pensjon.metrics.MetricsHelper
import no.nav.pensjon.tjeneste.LoggTjeneste
import no.nav.pensjon.util.scrable
import no.nav.pensjon.util.trunc
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.util.concurrent.CountDownLatch
import javax.annotation.PostConstruct

@Service
class KafkaLoggMeldingConsumer(
    private val loggTjeneste: LoggTjeneste,
    @Autowired(required = false) private val metricsHelper: MetricsHelper = MetricsHelper(SimpleMeterRegistry()) ) {

    private val log = LoggerFactory.getLogger(javaClass)
    private lateinit var kafkaCounter: MetricsHelper.Metric

    @PostConstruct
    fun initMetrics() {
        kafkaCounter = metricsHelper.init("sporingslogg_kafka")
    }

    private val latch = CountDownLatch(6)
    fun getLatch() = latch

    @KafkaListener(
        containerFactory = "onpremKafkaListenerContainerFactory",
        idIsGroup = false,
        topics = ["\${kafka.sporingslogg.topic}"],
        groupId = "\${kafka.sporingslogg.groupid}"
    )
    fun sporingsloggConsumer(hendelse: String, cr: ConsumerRecord<Int, String>, acknowledgment: Acknowledgment) {
        kafkaCounter.measure {
            log.info("*** Innkommende hendelse. Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()} ${ if (log.isDebugEnabled) ", hendelse: ${hendelse.trunc()}" else "" } ")

            val loggMelding: LoggMelding = try {
                LoggMelding.fromJson(hendelse)
            } catch (e: Exception) {
                log.error("Mottatt sporingsmelding kan ikke deserialiseres, må evt rettes og sendes inn på nytt.", e)
                acknowledgment.acknowledge()
                return@measure
            }

            try {
                validateRequest(loggMelding)
            } catch (sve: SporingsloggValidationException) {
                log.error("Mottatt sporingsmelding kan ikke valideres, må evt rettes og sendes inn på nytt.", sve)
                acknowledgment.acknowledge()
                return@measure
            }

            if (sjekkForIkkeGydldige(loggMelding)) {
                log.warn(
                    "Melding acket grunnet ikke gydlig, vi skipper denne." +
                    "Oracle.jdbc.OracleDatabaseException: ORA-01691: kan ikke utvide LOB-segment SPORINGSLOGG.SYS_LOB0000077564C00007$$ med 1024 i tabellområde SPORINGSLOGG")
                acknowledgment.acknowledge()
                return@measure
            }

            try {
                val loggInnslag = LoggInnslag.fromLoggMelding(LoggMelding.checkForAndEncode(loggMelding))
                val loggId = loggTjeneste.lagreLoggInnslag(loggInnslag)
                val melding = "ID: $loggId, person: ${loggMelding.person.scrable()}, tema: ${loggMelding.tema}, mottaker: ${loggMelding.mottaker}"

                log.info("Lagret melding med unik: $melding")
                acknowledgment.acknowledge()
                log.info("*** Acket, klar for neste loggmelding.. .")

            } catch (e: Exception) {
                log.error("Feilet ved lagre LoggInnslag", e)
                Thread.sleep(3000L) //sleep 3sek..
                throw Exception("Feilet ved lagre LoggInnslag", e)
            }

        }
        latch.countDown()
    }

    fun sjekkForIkkeGydldige(loggMelding: LoggMelding): Boolean = if (loggMelding.person == "01075534600") true else false

}