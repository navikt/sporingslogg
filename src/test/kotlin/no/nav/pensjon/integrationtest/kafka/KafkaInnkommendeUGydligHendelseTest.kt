package no.nav.pensjon.integrationtest.kafka

import no.nav.pensjon.TestApplication
import no.nav.pensjon.TestHelper.mockNoneValidLoggMeldingJson
import no.nav.pensjon.integrationtest.DataSourceTestConfig
import no.nav.pensjon.integrationtest.KafkaTestConfig
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles


@SpringBootTest( classes = [DataSourceTestConfig::class, KafkaTestConfig::class, TestApplication::class])
@ActiveProfiles("test")
@EnableMockOAuth2Server
@DirtiesContext
@EmbeddedKafka(topics = [TOPIC])
internal class KafkaInnkommendeUGydligHendelseTest: KafkaTests(){

    @Test
    fun `Naar en hendsle av LoggMelding er ugydlig skal det ikke lagers`() {
        val personIdent = "20903322123"

        assertEquals(0, loggTjeneste.hentAlleLoggInnslagForPerson(personIdent).size)

        val hendsleJson = mockNoneValidLoggMeldingJson()

        //send msg
        initTestRun().also {
                it.sendMsgOnDefaultTopic(hendsleJson)
                it.waitForlatch(kafkaLoggMeldingConsumer)
            }


        assertTrue(sjekkLoggingFinnes("Mottatt sporingsmelding kan ikke deserialiseres, må evt rettes og sendes inn på nytt."))
        assertEquals(0, loggTjeneste.hentAlleLoggInnslagForPerson(personIdent).size)

    }


}
