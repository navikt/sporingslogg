package no.nav.pensjon.listener

import io.micrometer.core.instrument.Metrics
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import jakarta.annotation.PostConstruct
import no.nav.pensjon.controller.LoggMeldingValidator.validateRequest
import no.nav.pensjon.controller.SporingsloggValidationException
import no.nav.pensjon.domain.LoggInnslag
import no.nav.pensjon.domain.LoggMelding
import no.nav.pensjon.metrics.MetricsHelper
import no.nav.pensjon.tjeneste.LoggTjeneste
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.CountDownLatch

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
        containerFactory = "aivenKafkaListenerContainerFactory",
        topics = ["\${kafka.sporingslogg.aiventopic}"],
        groupId = "\${kafka.sporingslogg.aivengroupid}"
    )
    fun sporingsloggConsumer(hendelse: String, cr: ConsumerRecord<Int, String>, acknowledgment: Acknowledgment) {
        MDC.putCloseable("x_request_id", UUID.randomUUID().toString()).use {
            kafkaCounter.measure {
                log.info("*** Innkommende hendelse. Offset: ${cr.offset()}, Partition: ${cr.partition()}, Key: ${cr.key()} ")

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

                try {
                    val loggInnslag = LoggInnslag.fromLoggMelding(LoggMelding.checkForAndEncode(loggMelding))
                    val loggId = loggTjeneste.lagreLoggInnslag(loggInnslag)

                    loggInnslag.tema?.let { countEnhet(it) } //metrics from who. .

                    val melding = "ID: $loggId, person: ${loggMelding.scramblePerson()}, tema: ${loggMelding.tema}, mottaker: ${loggMelding.mottaker}"
                    log.info("Lagret melding med unik: $melding")

                    log.debug("Loggelding lagret: TEMA: ${loggMelding.tema}, Grunnlag: ${loggMelding.behandlingsGrunnlag}, Mottaker: ${loggMelding.mottaker}, Leverandør: ${loggMelding.leverandoer}, Request: ${loggMelding.dataForespoersel}")

                    acknowledgment.acknowledge()
                    log.info("*** Acket, klar for neste loggmelding.. .")
                } catch (e: Exception) {
                    if (loggMelding.tema == "SYK" && checkForErrorMsgAndAck(e.message)) {
                        acknowledgment.acknowledge()
                        log.error("Feilet ved lagre LoggInnslag, må evt rettes og sendes inn på nytt, melding: ${e.message}", e)
                        return@measure
                    }
                    log.error("Feilet ved lagre LoggInnslag, melding: ${e.message}", e)
                    Thread.sleep(3000L) //sleep 3sek..
                    throw Exception("Feilet ved lagre LoggInnslag", e)
                }
            }
        }
        latch.countDown()
    }

    private fun checkForErrorMsgAndAck(errmsg: String?): Boolean {
        val msg = errmsg ?: return false
        return when {
            msg.contains("ORA-12899") -> true
            else -> false
        }
    }

    private fun countEnhet(tema: String) {
        try {
            Metrics.counter("LoggInnslag",   "tema", tema).increment()
        } catch (e: Exception) {
            log.warn("Metrics feilet på enhet: $tema")
        }
    }


}