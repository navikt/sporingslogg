package no.nav.pensjon.integrationtest.controller

import no.nav.pensjon.TestHelper.base64LevertData
import no.nav.pensjon.TestHelper.mockLoggMelding
import no.nav.pensjon.TestHelper.mockLoggMeldingAsJson
import no.nav.pensjon.integrationtest.BaseTests
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

internal class PostControllerTest: BaseTests() {

    @Test
    fun `Post gyldig loggmelding lagres i db med entraid`() {
        val brukerpid = "48886512234"

        loggTjeneste.lagreLoggInnslag(mockLoggMelding("19886512250"))
        loggTjeneste.lagreLoggInnslag(mockLoggMelding(brukerpid, samtykke = "DummyToken"))

        val token: String = mockEntraIdToken()
        val jsondata = mockLoggMeldingAsJson(brukerpid)

        val response = mockMvc.perform(
            MockMvcRequestBuilders.post("/sporingslogg/api/post")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content( jsondata ))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val result = response.response.getContentAsString(charset("UTF-8"))

        assertTrue(result.toLong() >= 3L)
        val sets = loggTjeneste.hentAlleLoggInnslagForPerson(brukerpid)
        println("Logginnslag: $sets")
        assertEquals(1, sets.size )

        sets.map {loggInnslag ->
            assertEquals(base64LevertData(), loggInnslag.leverteData)
        }
    }

    @Nested
    @DisplayName("Post med servicebruker")
    inner class PostServicebruker {

        @Test
        fun `Post gyldig loggmelding lagres i db med servicebruker`() {
            val brukerpid = "08886512234"

            loggTjeneste.lagreLoggInnslag(mockLoggMelding("12886512250"))
            loggTjeneste.lagreLoggInnslag(mockLoggMelding(brukerpid, samtykke = "DummyToken"))

            val token: String = mockServiceToken()
            val jsondata = mockLoggMeldingAsJson(brukerpid)

            val response = mockMvc.perform(
                    MockMvcRequestBuilders.post("/sporingslogg/api/post")
                        .header("Authorization", "Bearer $token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content( jsondata ))
                    .andExpect(MockMvcResultMatchers.status().isOk)
                    .andReturn()

            val result = response.response.getContentAsString(charset("UTF-8"))

            assertTrue(result.toLong() >= 3L)
            val sets = loggTjeneste.hentAlleLoggInnslagForPerson(brukerpid)
            println("Logginnslag: $sets")
            assertEquals(1, sets.size )

            sets.map {loggInnslag ->
                assertEquals(base64LevertData(), loggInnslag.leverteData)
            }
        }

        @Test
        fun `sjekk for postcontroller gyldig loggmelding ferdig base64 lagres i db`() {
            val brukerpid = "01884512234"
            val jsondata = mockLoggMeldingAsJson(brukerpid, samtykkeToken = "DummyToken")
            val token: String = mockServiceToken()

            val response = mockMvc.perform(
                MockMvcRequestBuilders.post("/sporingslogg/api/post")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content( jsondata ))
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andReturn()

            val result = response.response.getContentAsString(charset("UTF-8"))

            assertTrue(result.toLong() >= 1L)
            val sets = loggTjeneste.hentAlleLoggInnslagForPerson(brukerpid)
            assertEquals(1, sets.size )

        }

        @Test
        fun `sjekk postcontroller ugyldig loggmelding sendes inn`() {
            val brukerpid = "08886512234"
            val jsondata = mockLoggMeldingAsJson(brukerpid, mottaker = "123123")
            val token: String = mockServiceToken()

            val response = mockMvc.perform(
                MockMvcRequestBuilders.post("/sporingslogg/api/post")
                    .header("Authorization", "Bearer $token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content( jsondata ))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError)
                .andReturn()

            val result = response.response.errorMessage
            assertEquals("Mottaker must be of length 9", result)
        }

    }

}