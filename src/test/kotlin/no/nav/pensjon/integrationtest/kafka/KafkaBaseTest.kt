package no.nav.pensjon.integrationtest.kafka

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.clearAllMocks
import no.nav.pensjon.TestHelper.mockNoneValidLoggMeldingJson
import no.nav.pensjon.listener.KafkaLoggMeldingConsumer
import no.nav.pensjon.tjeneste.LoggTjeneste
import org.junit.jupiter.api.AfterEach
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import java.util.concurrent.TimeUnit

const val TOPIC = "aiven-sporingslogg-loggmeldingMottatt" //"aapen-sporingslogg-loggmeldingMottatt"

/***
 * Tatt fra eessi-pensjon-journalforing
 * https://github.com/navikt/eessi-pensjon-journalforing/blob/master/src/test/kotlin/no/nav/eessi/pensjon/integrasjonstest/IntegrasjonsBase.kt
 */
abstract class KafkaListenerTest {

    @Autowired
    private lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Autowired
    private lateinit var consumerFactory: ConsumerFactory<String, String>

    @Autowired
    private lateinit var producerFactory: ProducerFactory<String, String>

    @Autowired
    protected lateinit var loggTjeneste: LoggTjeneste

    @Autowired
    protected lateinit var kafkaLoggMeldingConsumer: KafkaLoggMeldingConsumer

    private val deugLogger: Logger = LoggerFactory.getLogger("no.nav.pensjon") as Logger
    private val listAppender = ListAppender<ILoggingEvent>()

    @AfterEach
    fun after() {
        println("************************* CLEANING UP AFTER CLASS*****************************")
        clearAllMocks()
        embeddedKafka.kafkaServers.forEach { it.shutdown() }
        listAppender.stop()
    }

    protected fun debugPrintLogging() {
        println("==******************************************==")
        println("Size: ${listAppender.list.size}")
        listAppender.list.map { logEvent ->
            println(logEvent.message)
        }
        println("--******************************************--")
    }

    protected fun sjekkLoggingFinnes(keywords: String): Boolean {
        val logsList: List<ILoggingEvent> = listAppender.list
        val result : String? = logsList.find { message ->
            message.message.contains(keywords)
        }?.message
        return result?.contains(keywords) ?: false
    }

    private fun settOppUtitlityConsumer(): KafkaMessageListenerContainer<String, String> {
        val consumerProperties = KafkaTestUtils.consumerProps(
            "KC-$TOPIC",
            "false",
            embeddedKafka
        )
        consumerProperties["auto.offset.reset"] = "earliest"
        val container = KafkaMessageListenerContainer(consumerFactory, ContainerProperties(TOPIC))
        container.setupMessageListener(
            MessageListener<String, String> { record -> println("Konsumerer melding:  $record") }
        )
        return container
    }

    protected fun initAndRunContainer(timeout: Long = 10): TestResult {
        println("*************************  INIT START *****************************")

        listAppender.start()
        deugLogger.addAppender(listAppender)

        val container = settOppUtitlityConsumer()
        container.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.partitionsPerTopic)

        println("*************************  INIT DONE *****************************")
        val template = KafkaTemplate(producerFactory).apply { defaultTopic = TOPIC }
        return TestResult(template, container, timeout)
    }

    data class TestResult(
        val kafkaTemplate: KafkaTemplate<String, String>,
        val container: KafkaMessageListenerContainer<String, String>,
        val timeout: Long
    ) {

        fun sendMsgOnDefaultTopic(hendelseAsJson : String) {
           kafkaTemplate.sendDefault(hendelseAsJson).get()
        }

        fun waitForlatch(kafkaConsumer: KafkaLoggMeldingConsumer) = kafkaConsumer.getLatch().await(timeout, TimeUnit.SECONDS)
    }

    protected fun mockNoValidJson() : String = mockNoneValidLoggMeldingJson()

}