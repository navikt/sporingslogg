package no.nav.pensjon

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pensjon.domain.LoggInnslag
import no.nav.pensjon.domain.LoggMelding
import java.time.LocalDateTime

object TestHelper {

    fun mockLoggInnslag(ident: String = "11886512234") : LoggInnslag {
        val tidspunkt  = LocalDateTime.of(2021, 10, 9, 10, 10)
        return LoggInnslag(
            person = ident,
            mottaker = "938908909",
            tema = "PEN",
            hjemmel = "Lovhjemmel samordningsloven § 27 (samordningsloven paragraf 27)",
            uthentingsTidspunkt = tidspunkt,
            leverteData = base64LevertData(),
            samtykkeToken =  "DummyToken",
            foresporsel = "Foresporsel",
            leverandor = "lever"
        )
    }

    fun mockLoggMelding(ident: String = "11886512234", mottaker: String = "938908909", tema: String = "PEN", levertBase64: Boolean = false): LoggMelding {
        val tidspunkt  = LocalDateTime.of(2021, 10, 9, 10, 10)
        return LoggMelding(
            id = null,
            person = ident,
            mottaker = mottaker,
            tema = tema,
            behandlingsGrunnlag = "Lovhjemmel samordningsloven § 27 (samordningsloven paragraf 27)",
            uthentingsTidspunkt = tidspunkt,
            leverteData = if (levertBase64) base64LevertData() else "LeverteData er kun for dummyTesting av sporingslogg Test",
            samtykkeToken = null,
            leverandoer = null,
            dataForespoersel = null
        )
    }

    fun mockLoggMeldingAsJson(ident: String = "11886512234", mottaker: String = "938908909", tema: String = "PEN",  levertBase64: Boolean = false) = mapAnyToJson(mockLoggMelding(ident, mottaker, tema, levertBase64))


    fun mockNoneValidLoggMeldingJson() : String {
        return """
            {
              "id": 12,
              "title": "Brown Perfume",
              "description": "Royal_Mirage Sport Brown Perfu",
              "price": 40,
              "discountPercentage": 15.66,
              "rating": 4,
              "stock": 52
            }
        """.trimIndent()
    }


    fun base64LevertData(): String = "TGV2ZXJ0ZURhdGEgZXIga3VuIGZvciBkdW1teVRlc3RpbmcgYXYgc3BvcmluZ3Nsb2dnIFRlc3Q="

    fun mapAnyToJson(data: Any): String {
        @Suppress("CAST_NEVER_SUCCEEDS")
        return jacksonObjectMapper()
            .registerModule(JavaTimeModule() as Module)
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(data)
    }

}

