package no.nav.pensjon.integrationtest

import com.nimbusds.jose.JOSEObjectType
import io.mockk.clearAllMocks
import no.nav.pensjon.TestApplication
import no.nav.pensjon.controller.TokenHelper.Issuer.SERVICEBRUKER
import no.nav.pensjon.controller.TokenHelper.Issuer.TOKENDINGS
import no.nav.pensjon.integrationtest.kafka.TOPIC
import no.nav.pensjon.tjeneste.LoggTjeneste
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import java.util.UUID
import kotlin.text.lowercase
import kotlin.to

@SpringBootTest(classes = [DataSourceTestConfig::class, TestApplication::class, KafkaTestConfig::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["test"])
@EnableMockOAuth2Server
@AutoConfigureMockMvc
@EmbeddedKafka(topics = [TOPIC + "LESPOSTTST"])
abstract class BaseTests {

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

    fun mockEntraIdToken(): String = token2(
        issuerId = "entraid",
        audience = listOf("tp", "entraid-test"),
        claims = mapOf(
            "azp_name" to UUID.randomUUID().toString(),
            "idtyp" to "app",
            "azp_name" to "MockOAuth2Server",
        )
    )

    private fun token2(
        issuerId: String,
        audience: List<String>,
        subject: String = UUID.randomUUID().toString(),
        claims: Map<String, Any>): String {

        return server.issueToken(
            issuerId = issuerId,
            clientId = "test-client",
            tokenCallback = DefaultOAuth2TokenCallback(
                issuerId = issuerId,
                typeHeader = JOSEObjectType.JWT.type,
                audience = audience,
                subject = subject,
                claims = claims,
                expiry = 3322L
            )
        ).serialize()
    }


}