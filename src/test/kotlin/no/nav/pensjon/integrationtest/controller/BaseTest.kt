package no.nav.pensjon.integrationtest.controller

import com.nimbusds.jose.JOSEObjectType
import io.mockk.clearAllMocks
import no.nav.pensjon.TestApplication
import no.nav.pensjon.integrationtest.DataSourceTestConfig
import no.nav.pensjon.integrationtest.KafkaTestConfig
import no.nav.pensjon.tjeneste.LoggTjeneste
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.support.KafkaHeaders.TOPIC
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc


@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(classes = [DataSourceTestConfig::class, KafkaTestConfig::class, TestApplication::class], value = ["SPRING_PROFILES_ACTIVE", "unsecured-webmvctest"], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["unsecured-webmvctest"])
@EmbeddedKafka(topics = [TOPIC+"LESPOSTTST"])
@EnableMockOAuth2Server
@DirtiesContext
@AutoConfigureMockMvc
abstract class BaseTest {

    @Autowired
    protected lateinit var loggTjeneste: LoggTjeneste

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @Autowired
    protected lateinit var server: MockOAuth2Server

    @AfterEach
    fun takeDown() {
        clearAllMocks()
    }

    fun mockTokenDings(subject: String): String = token("tokendings", subject.hashCode().toString(), "tokendings-test", mapOf("acr" to  "Level4", "pid" to subject))

    fun mockServiceToken() = token("servicebruker",  "srvsporingslogg", "srvsporingslogg")

    private fun token(issuerId: String, subject: String, audience: String): String {
        return token(issuerId, subject, audience, emptyMap())
    }
    private fun token(issuerId: String, subject: String, audience: String, claims: Map<String, Any>): String {
        return server.issueToken(
            issuerId, "theclientid", DefaultOAuth2TokenCallback(
                issuerId, subject, JOSEObjectType.JWT.type, listOf(audience), claims, 3600
            )
        ).serialize()
    }

}