package no.nav.pensjon.integrationtest.controller

import no.nav.pensjon.metrics.MetricsHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


internal class ActuatorTest: BaseTest() {

    @Autowired
    private lateinit var metricsHelper: MetricsHelper

    private lateinit var metric : MetricsHelper.Metric

    @BeforeEach
    fun setup() {
        metric = metricsHelper.init("dummyles")
    }

    @Test
    fun `sjekk for postcontroller gyldig loggmelding for post og lagring db`() {

        repeat(3) {
            metric.measure {
                repeat(3) {
                    (52*32*21*1*3/7)/3
                }
            }
        }

        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/actuator/health")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val result = response.response.getContentAsString(charset("UTF-8"))
        assertEquals("{\"status\":\"UP\"}", result)
    }

}