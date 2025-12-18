package no.nav.pensjon.integrationtest.controller

import no.nav.pensjon.TestHelper.mockLoggMelding
import no.nav.pensjon.domain.LoggMelding
import no.nav.pensjon.integrationtest.BaseTests
import no.nav.pensjon.util.fromJson2Any
import no.nav.pensjon.util.typeRefs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

internal class FinnControllerTests: BaseTests() {

    @Test
    fun `sjekk finnController for sok etter person`() {
        loggTjeneste.lagreLoggInnslag(mockLoggMelding("13055212250"))
        loggTjeneste.lagreLoggInnslag(mockLoggMelding("13055220123"))
        loggTjeneste.lagreLoggInnslag(mockLoggMelding("17024101234"))
        val token: String = mockServiceToken()

        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/sporingslogg/api/test/finn/")
                .header("Authorization", "Bearer $token")
                .header("ident", "130552")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val result = response.response.getContentAsString(charset("UTF-8"))
        assertEquals("""["13055212250","13055220123"]""", result)

    }

    @Test
    fun `sjekk finnController for sok etter person med ingen resultat`() {
        val token: String = mockServiceToken()
        val test = "test"

        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/sporingslogg/api/test/finn/")
                .header("Authorization", "Bearer $token")
                .header("ident", "010203404")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val result = response.response.getContentAsString(charset("UTF-8"))
        assertEquals("[]", result)

        assertEquals("test", test)

    }

    @Test
    fun `sjekk finnController for hent av data`() {
        loggTjeneste.lagreLoggInnslag(mockLoggMelding("03055212288", samtykke = "DummyToken"))
        val token: String = mockServiceToken()

        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/sporingslogg/api/test/hent/")
                .header("Authorization", "Bearer $token")
                .header("ident", "03055212288")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val result = response.response.getContentAsString(charset("UTF-8"))
        val list: List<LoggMelding> = fromJson2Any(result, typeRefs())

        val expected = """
            LoggMelding [person=030552xxxxx, mottaker=938908909, tema=PEN, behandlingsGrunnlag=Lovhjemmel samordningsloven § 27 (samordningsloven paragraf 27), uthentingsTidspunkt=2021-10-09T10:10, leverteData=TGV2ZXJ0ZURhdGEgZXIga3VuIGZvciBkdW1teVRlc3RpbmcgYXYgc3BvcmluZ3Nsb2dnIFRlc3Q=, samtykkeToken=DummyToken, dataForespoersel=null, leverandoer=null]
        """.trimIndent()

        assertEquals(expected, list[0].toString())

    }

    @Test
    fun `sjekk finnController for hentAntall`() {
        loggTjeneste.lagreLoggInnslag(mockLoggMelding("01053212288"))
        loggTjeneste.lagreLoggInnslag(mockLoggMelding("01053212288"))
        val token: String = mockServiceToken()

        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/sporingslogg/api/test/antall/")
                .header("Authorization", "Bearer $token")
                .header("ident", "01053212288")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        assertEquals("2", response.response.getContentAsString(charset("UTF-8")))

    }

}