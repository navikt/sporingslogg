package no.nav.pensjon.integrationtest.controller

import com.nimbusds.jose.JOSEObjectType
import io.mockk.clearAllMocks
import no.nav.pensjon.TestApplication
import no.nav.pensjon.controller.TokenHelper.Issuer.SERVICEBRUKER
import no.nav.pensjon.controller.TokenHelper.Issuer.TOKENDINGS
import no.nav.pensjon.integrationtest.DataSourceTestConfig
import no.nav.pensjon.integrationtest.KafkaTestConfig
import no.nav.pensjon.integrationtest.kafka.TOPIC
import no.nav.pensjon.tjeneste.LoggTjeneste
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest(classes = [DataSourceTestConfig::class, KafkaTestConfig::class, TestApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["test"])
@EnableMockOAuth2Server
@AutoConfigureMockMvc
@EmbeddedKafka(topics = [TOPIC + "LESPOSTTST"])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class BaseTest {

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var loggTjeneste: LoggTjeneste

    @Autowired
    protected lateinit var server: MockOAuth2Server

    @AfterEach
    fun takeDown() {
        clearAllMocks()
    }

    fun mockTokenDings(subject: String): String = token(
        TOKENDINGS.name.lowercase(),
        subject.hashCode().toString(),
        "tokendings-test",
        mapOf("acr" to  "Level4", "pid" to subject)
    )

    fun mockServiceToken() = token(
        SERVICEBRUKER.name.lowercase(),
        "srvsporingslogg",
        "srvsporingslogg",
        emptyMap()
    )

    private fun token(
        issuerId: String,
        subject: String,
        audience: String,
        claims: Map<String, Any>): String {

        return server.issueToken(
            issuerId, "theclientid", DefaultOAuth2TokenCallback(
                issuerId, subject, JOSEObjectType.JWT.type, listOf(audience), claims, 3600
            )
        ).serialize()
    }

}