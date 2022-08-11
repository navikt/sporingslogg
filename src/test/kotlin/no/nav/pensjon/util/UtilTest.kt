package no.nav.pensjon.util

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonProcessingException
import no.nav.pensjon.TestHelper.mockLoggMeldingAsJson
import no.nav.pensjon.TestHelper.mockNoneValidLoggMeldingJson
import no.nav.pensjon.domain.LoggMelding
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UtilTest {

    @Test
    fun `scramble pinid len 11 to 6 trailing x on valid pid`() {
        assertEquals("123456xxxxx", "12345678901".scrable())
    }

    @Test
    fun `scramble pinid from 20 to 15 with trailing x on noneValid pid`() {
        assertEquals("123456789012345xxxxx", "12345678901234567890".scrable())
    }

    @Test
    fun `scramble pinid noneValid pid`() {
        assertEquals("12", "12".scrable())
    }

    @Test
    fun `scramble on null`() {
        assertEquals(null , null.scrable())
    }

    @Test
    fun `from valid Json to LoggMelding`() {

        val json = mockLoggMeldingAsJson("23193951223")
        val loggMelding = LoggMelding.fromJson(json)

        assertEquals(LoggMelding::class.java, loggMelding.javaClass)
        assertEquals("23193951223", loggMelding.person)

    }

    @Test
    fun `from noneValid Json to LoggMelding`() {

        val json = mockNoneValidLoggMeldingJson()
        assertThrows<JsonProcessingException> {
           LoggMelding.fromJson(json)
        }
    }

    @Test
    fun `from loggMelding json with wrong values`() {

        val json = """
            { "person": "11886512234", "mottaker": 938908909, tema: "PEN", "leverteData": 10, "behandlingsGrunnlag": "ikke tomt men tull" }
        """.trimIndent()

        assertThrows<JsonParseException> {
            LoggMelding.fromJson(json)
        }

    }


}