package no.nav.pensjon.integrationtest.controller

import com.ninjasquad.springmockk.MockkBean
import io.mockk.clearAllMocks
import no.nav.pensjon.UnsecuredWebMvcTestLauncher
import no.nav.pensjon.controller.TokenHelper
import no.nav.pensjon.integrationtest.DataSourceTestConfig
import no.nav.pensjon.integrationtest.KafkaTestConfig
import no.nav.pensjon.tjeneste.LoggTjeneste
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
@SpringBootTest(classes = [DataSourceTestConfig::class, KafkaTestConfig::class, UnsecuredWebMvcTestLauncher::class], value = ["SPRING_PROFILES_ACTIVE", "unsecured-webmvctest"], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = ["unsecured-webmvctest"])
@EmbeddedKafka(topics = [TOPIC+"LESPOSTTST"])
@DirtiesContext
@AutoConfigureMockMvc
abstract class BaseTest {

    @MockkBean
    protected lateinit var tokenHelper: TokenHelper

    @Autowired
    protected lateinit var loggTjeneste: LoggTjeneste

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @AfterEach
    fun takeDown() {
        clearAllMocks()
    }


}