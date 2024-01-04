package no.nav.pensjon.integrationtest.kafka

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.clearAllMocks
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

@Suppress("SpringJavaInjectionPointsAutowiringInspection")
open class KafkaTests {

    @Autowired
    protected lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Autowired
    protected lateinit var consumerFactory: ConsumerFactory<String, String>

    @Autowired
    protected lateinit var producerFactory: ProducerFactory<String, String>

    @Autowired
    protected lateinit var kafkaLoggMeldingConsumer: KafkaLoggMeldingConsumer

    @Autowired
    protected lateinit var loggTjeneste: LoggTjeneste

    private val deugLogger: Logger = LoggerFactory.getLogger("no.nav.pensjon") as Logger
    private val listAppender = ListAppender<ILoggingEvent>()

    @AfterEach
    fun after() {
        clearAllMocks()
        listAppender.stop()
        embeddedKafka.destroy()
    }

    protected fun sjekkLoggingFinnes(keywords: String): Boolean {
        val logsList: List<ILoggingEvent> = listAppender.list
        val result : String? = logsList.find { message ->
            message.message.contains(keywords)
        }?.message
        return result?.contains(keywords) ?: false
    }

    protected fun debugPrintLogging() {
        println("==******************************************==")
        println("Size: ${listAppender.list.size}")
        listAppender.list.map { logEvent ->
            println(logEvent.message)
        }
        println("--******************************************--")
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

    protected fun initTestRun(timeout: Long = 10L): TestResult {
        println("*** INIT START ***")

        listAppender.start()
        deugLogger.addAppender(listAppender)
        val container = settOppUtitlityConsumer()
        container.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.partitionsPerTopic)

        val template = KafkaTemplate(producerFactory).apply { defaultTopic =  TOPIC }

        return TestResult(template, container, timeout)
    }

    protected data class TestResult(
        val kafkaTemplate: KafkaTemplate<String, String>,
        val container: KafkaMessageListenerContainer<String, String>,
        val timeout: Long
    ) {

        fun sendMsgOnDefaultTopic(hendelseAsJson : String) {
            kafkaTemplate.sendDefault(hendelseAsJson).get()
        }
        fun waitForlatch(kafkaConsumer: KafkaLoggMeldingConsumer) = kafkaConsumer.getLatch().await(timeout, TimeUnit.SECONDS)

    }


}