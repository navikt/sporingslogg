package no.nav.pensjon.integrationtest.kafka

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.read.ListAppender
import io.mockk.clearAllMocks
import no.nav.pensjon.integrationtest.BaseTests
import no.nav.pensjon.listener.KafkaLoggMeldingConsumer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.TestInstance
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
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class KafkaTests: BaseTests() {

    @Autowired
    protected lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Autowired
    protected lateinit var consumerFactory: ConsumerFactory<String, String>

    @Autowired
    protected lateinit var producerFactory: ProducerFactory<String, String>

    @Autowired
    protected lateinit var kafkaLoggMeldingConsumer: KafkaLoggMeldingConsumer

    private val debugLogger: Logger = LoggerFactory.getLogger("no.nav.pensjon") as Logger
    private val listAppender = ListAppender<ILoggingEvent>()

    @AfterEach
    fun after() {
        listAppender.stop()
        embeddedKafka.destroy()
        clearAllMocks()
    }

    fun sjekkLoggingFinnes(keywords: String): Boolean {
        val logsList: List<ILoggingEvent> = listAppender.list
        val result : String? = logsList.find { message ->
            message.message.contains(keywords)
        }?.message
        return result?.contains(keywords) ?: false
    }

    fun debugPrintLogging() {
        println("==******************************************==")
        println("Size: ${listAppender.list.size}")
        listAppender.list.map { logEvent ->
            println(logEvent.message)
        }
        println("--******************************************--")
    }

    private fun settOppUtitlityConsumer(): KafkaMessageListenerContainer<String, String> {
        val consumerProperties = KafkaTestUtils.consumerProps(
                embeddedKafka,
                "KC-$TOPIC",
                false
            )
        consumerProperties["auto.offset.reset"] = "earliest"
        val container = KafkaMessageListenerContainer(consumerFactory, ContainerProperties(TOPIC))
        container.setupMessageListener(
            MessageListener<String, String> { record -> println("Konsumerer melding:  $record") }
        )
        return container
    }

    fun initTestRun(timeout: Long = 10L): TestResult {
        println("*** INIT START ***")

        listAppender.start()
        debugLogger.addAppender(listAppender)
        val container = settOppUtitlityConsumer()
        container.start()
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.partitionsPerTopic)

        val template = KafkaTemplate(producerFactory).apply { setDefaultTopic(TOPIC) }

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


}