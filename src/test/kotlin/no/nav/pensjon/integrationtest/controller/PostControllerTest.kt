package no.nav.pensjon.integrationtest.controller

import no.nav.pensjon.TestHelper.base64LevertData
import no.nav.pensjon.TestHelper.mockLoggInnslag
import no.nav.pensjon.TestHelper.mockLoggMeldingAsJson
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers


internal class PostControllerTest: BaseTest() {

    @Test
    fun `sjekk for postcontroller gyldig loggmelding for post og lagring db`() {
        val brukerpid = "08886512234"

        loggTjeneste.lagreLoggInnslag(mockLoggInnslag("12886512250"))
        loggTjeneste.lagreLoggInnslag(mockLoggInnslag(brukerpid))

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
        assertEquals(2, sets.size )

        sets.map {loggInnslag ->
            assertEquals(base64LevertData(), loggInnslag.leverteData)
        }
    }

    @Test
    fun `sjekk for postcontroller gyldig loggmelding ferdig base64 lagres i db`() {
        val brukerpid = "01884512234"
        val jsondata = mockLoggMeldingAsJson(brukerpid, levertBase64 = true)
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