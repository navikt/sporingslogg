package no.nav.pensjon.integrationtest.kafka

import no.nav.pensjon.TestApplication
import no.nav.pensjon.TestHelper.mapAnyToJson
import no.nav.pensjon.TestHelper.mockLoggMelding
import no.nav.pensjon.TestHelper.mockNoneValidAAPMelding
import no.nav.pensjon.TestHelper.mockNoneValidLoggMeldingJson
import no.nav.pensjon.integrationtest.DataSourceTestConfig
import no.nav.pensjon.integrationtest.KafkaTestConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles

@SpringBootTest( classes = [DataSourceTestConfig::class, KafkaTestConfig::class, TestApplication::class])
@ActiveProfiles("test")
@DirtiesContext
@EmbeddedKafka(topics = [TOPIC])
internal class KafkaInnkommendeFlereHendelserTest: KafkaTests() {


    @Test
    fun `Naar en flere hendelser er gyldige og ugyldige skal gyldige lagres i db`() {

        assertEquals(0, loggTjeneste.countAlleLogginnslag())

        val loggMelding = mockLoggMelding("20903322123", samtykke = "DummyToken")
        val hendsleJson1 = mapAnyToJson(loggMelding)
        val hendsleJson2 = mapAnyToJson(loggMelding.copy(person = "10805322123" , mottaker = "123456789"))
        val hendsleNotValidert = mapAnyToJson(loggMelding.copy(mottaker = "12345"))
        val hendsleUgydligJson = mockNoneValidLoggMeldingJson()
        val hendsleAAPUgydligJson = mockNoneValidAAPMelding()

        initTestRun().also { runTest ->
            runTest.sendMsgOnDefaultTopic(hendsleJson1)
            runTest.sendMsgOnDefaultTopic(hendsleUgydligJson)
            runTest.sendMsgOnDefaultTopic(hendsleNotValidert)
            runTest.sendMsgOnDefaultTopic(hendsleAAPUgydligJson)
            runTest.sendMsgOnDefaultTopic(hendsleJson2)
            runTest.waitForlatch(kafkaLoggMeldingConsumer)
        }

        //debugPrintLogging()

        assertTrue(sjekkLoggingFinnes("Lagret melding med unik: ID: 1, person: 209033xxxxx, tema: PEN, mottaker: 938908909"))
        assertTrue(sjekkLoggingFinnes("Mottatt sporingsmelding kan ikke valideres, må evt rettes og sendes inn på nytt."))
        assertTrue(sjekkLoggingFinnes("Lagret melding med unik: ID: 2, person: 174687xxxxx, tema: AAP, mottaker: 938908909"))
        assertTrue(sjekkLoggingFinnes("Lagret melding med unik: ID: 3, person: 108053xxxxx, tema: PEN, mottaker: 123456789"))
        assertTrue(sjekkLoggingFinnes("Mottatt sporingsmelding kan ikke valideres, må evt rettes og sendes inn på nytt."))
        assertEquals(3, loggTjeneste.countAlleLogginnslag())
    }

}
