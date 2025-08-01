package no.nav.pensjon.integrationtest.kafka

import no.nav.pensjon.TestApplication
import no.nav.pensjon.TestHelper.mapAnyToJson
import no.nav.pensjon.TestHelper.mockLoggMelding
import no.nav.pensjon.integrationtest.DataSourceTestConfig
import no.nav.pensjon.integrationtest.KafkaTestConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional


@SpringBootTest( classes = [DataSourceTestConfig::class, KafkaTestConfig::class, TestApplication::class])
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(topics = [TOPIC])
internal class KafkaInnkommendeGydligHendelseTest: KafkaTests() {

    @Test
    @Transactional
    fun `Naar en hendsle av LoggMelding er gyldig Saa skal det lagres til db`() {
        val personIdent = "20903322123"

        val mockLoggMelding = mockLoggMelding(personIdent, samtykke = "DummyToken")
        val hendsleJson = mapAnyToJson(mockLoggMelding)


        initTestRun().also {
            it.sendMsgOnDefaultTopic(hendsleJson)
            it.waitForlatch(kafkaLoggMeldingConsumer)
        }

        val loggInnslagList = loggTjeneste.hentAlleLoggInnslagForPerson(personIdent)
        assertEquals(1, loggInnslagList.size)
        val loggInnslag = loggInnslagList.first()

        assertTrue(sjekkLoggingFinnes("Lagret melding med unik: ID: 1, person: 209033xxxxx, tema: PEN, mottaker: 938908909"))
        assertTrue(sjekkLoggingFinnes("*** Acket, klar for neste loggmelding.. ."))
        assertEquals(mockLoggMelding.tema, loggInnslag.tema)
        assertEquals(mockLoggMelding.mottaker, loggInnslag.mottaker)
        assertEquals(mockLoggMelding.uthentingsTidspunkt, loggInnslag.uthentingsTidspunkt)

    }



}
