package no.nav.pensjon.integrationtest.controller

import io.mockk.every
import no.nav.pensjon.TestHelper.mockLoggInnslag
import no.nav.pensjon.domain.LoggMelding
import no.nav.pensjon.util.fromJson2Any
import no.nav.pensjon.util.typeRefs
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers

internal class LesControllerTest: BaseTest() {

    @Test
    fun `sjekk for lescontroller gyldig person funnet return liste over data`() {
        val personIdent = "11886512250"
        every { tokenHelper.getPid() } returns personIdent

        loggTjeneste.lagreLoggInnslag(mockLoggInnslag(personIdent))
        val preChecklist = loggTjeneste.hentAlleLoggInnslagForPerson(personIdent)
        assertEquals(1, preChecklist.size)

        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/sporingslogg/api/les")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val result = response.response.getContentAsString(charset("UTF-8"))
        val list: List<LoggMelding> =  fromJson2Any(result, typeRefs())

        val expected = """
            LoggMelding [person=118865xxxxx, mottaker=938908909, tema=PEN, behandlingsGrunnlag=Lovhjemmel samordningsloven § 27 (samordningsloven paragraf 27), uthentingsTidspunkt=2021-10-09T10:10, leverteData=TGV2ZXJ0ZURhdGEgZXIga3VuIGZvciBkdW1teVRlc3RpbmcgYXYgc3BvcmluZ3Nsb2dnIFRlc3Q=, samtykkeToken=DummyToken, dataForespoersel=Foresporsel, leverandoer=lever]
        """.trimIndent()

        assertEquals(1, list.size)
        assertEquals(expected, list[0].toString())

    }

    @Test
    fun `sjekk for lescontroller ingen persondata funnet return tom liste` () {
        every { tokenHelper.getPid() } returns "20883234332"

        loggTjeneste.lagreLoggInnslag(mockLoggInnslag("1188651431"))

        val response = mockMvc.perform(
            MockMvcRequestBuilders.get("/sporingslogg/api/les")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()

        val result = response.response.getContentAsString(charset("UTF-8"))
        val list: List<LoggMelding> =  fromJson2Any(result, typeRefs())

        assertEquals(0, list.size)

    }

}