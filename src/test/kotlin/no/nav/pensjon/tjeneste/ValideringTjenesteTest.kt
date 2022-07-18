package no.nav.pensjon.tjeneste

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ValideringTjenesteTest {

    @Test
    fun `validate text shorter than max length`() {
        ValideringTjeneste.validerMaxLengde("ikke noe her", 15, "Maks lengde 15")
        ValideringTjeneste.validerMaxLengde("", 15, "Maks lengde 15")
    }

    @Test
    fun `validate text longer than max lengt`() {
        assertThrows<IllegalArgumentException> {
            ValideringTjeneste.validerMaxLengde("ikke noe her", 5, "Maks lengde 5")
        }
        try {
            ValideringTjeneste.validerMaxLengde("ikke noe her, ikke noe her, ikke noe her, ikke noe her, ikke noe her, ikke noe her, ikke noe her, ikke noe her", 5, "Maks lengde 5")
        } catch (iae: IllegalArgumentException) {
            Assertions.assertTrue(iae.message!!.contains("...."))
        }
    }

    @Test
    fun `validate valid not blank text`() {
        ValideringTjeneste.validerIkkeBlank("Ikke noe her", "Kan ike være blank")
    }

    @Test
    fun `validate blank text`() {
        assertThrows<java.lang.IllegalArgumentException> {
            ValideringTjeneste.validerIkkeBlank("", "Kan ike være blank")
        }
        assertThrows<java.lang.IllegalArgumentException> {
            ValideringTjeneste.validerIkkeBlank(null, "Kan ike være blank")
        }
    }


}