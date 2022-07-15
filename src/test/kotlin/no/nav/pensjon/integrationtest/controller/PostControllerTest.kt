package no.nav.pensjon.integrationtest.controller

import io.mockk.every
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
        every { tokenHelper.getSystemUserId() } returns "srvsporingslogg"

        loggTjeneste.lagreLoggInnslag(mockLoggInnslag("12886512250"))
        loggTjeneste.lagreLoggInnslag(mockLoggInnslag(brukerpid))
        println(loggTjeneste.hentAlleLoggInnslagForPerson(brukerpid))

        val jsondata = mockLoggMeldingAsJson(brukerpid)

        val response = mockMvc.perform(
                MockMvcRequestBuilders.post("/sporingslogg/post")
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


}