package no.nav.pensjon.listener

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.pensjon.TestHelper.mapAnyToJson
import no.nav.pensjon.TestHelper.mockLoggMelding
import no.nav.pensjon.TestHelper.mockLoggMeldingAsJson
import no.nav.pensjon.TestHelper.mockNoneValidLoggMeldingJson
import no.nav.pensjon.tjeneste.LoggTjeneste
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.kafka.support.Acknowledgment

internal class KafkaLoggMeldingConsumerTest {

    val loggTjeneste : LoggTjeneste = mockk(relaxed = true)
    val acknowledgment: Acknowledgment = mockk(relaxed = true)
    val kafkaLoggMeldingConsumer = KafkaLoggMeldingConsumer(loggTjeneste)

    @BeforeEach
    fun setup() {
        kafkaLoggMeldingConsumer.initMetrics()
    }

    @Test
    fun testingAvConsumer() {

        val hendelse = mockLoggMeldingAsJson()
        every { loggTjeneste.lagreLoggInnslag(any()) } returns 100L
        kafkaLoggMeldingConsumer.sporingsloggConsumer(hendelse, mockk(relaxed = true), acknowledgment)
        verify(exactly = 1) { acknowledgment.acknowledge() }
    }

    @Test
    fun testingAvConsumerSomFeilerVedLagring() {

        val hendelse = mockLoggMeldingAsJson()

        every { loggTjeneste.lagreLoggInnslag(any()) } throws IllegalArgumentException("Kan ikke ta imot verdi for")

        assertThrows<Exception> {
            kafkaLoggMeldingConsumer.sporingsloggConsumer(hendelse, mockk(relaxed = true), acknowledgment)
        }

        verify(exactly = 0) { acknowledgment.acknowledge() }


    }

    @Test
    fun testingFeiletAvConsumer() {

        every { loggTjeneste.lagreLoggInnslag(any()) } returns 100L

        val mockJson = mockNoneValidLoggMeldingJson()

        kafkaLoggMeldingConsumer.sporingsloggConsumer(mockJson, mockk(relaxed = true), acknowledgment)

        verify(exactly = 1) { acknowledgment.acknowledge() }

    }

    @Test
    fun testingValideringFeiler() {

        val mockLoggMelding = mockLoggMelding("03064923456").copy(mottaker = "1234", tema = "PENS")
        val json = mapAnyToJson(mockLoggMelding)

        val acknowledgment: Acknowledgment = mockk(relaxed = true)
       kafkaLoggMeldingConsumer.sporingsloggConsumer(json, mockk(relaxed = true), acknowledgment)

        verify(exactly = 1) { acknowledgment.acknowledge() }

    }


}