package no.nav.pensjon

import com.fasterxml.jackson.databind.Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.pensjon.domain.LoggInnslag
import no.nav.pensjon.domain.LoggMelding
import java.time.LocalDateTime

object TestHelper {

    fun mockLoggInnslag(ident: String = "11886512234", samtykke: String? = "DummyToken") : LoggInnslag {
        val tidspunkt  = LocalDateTime.of(2021, 10, 9, 10, 10)
        return LoggInnslag(
            person = ident,
            mottaker = "938908909",
            tema = "PEN",
            hjemmel = "Lovhjemmel samordningsloven § 27 (samordningsloven paragraf 27)",
            uthentingsTidspunkt = tidspunkt,
            leverteData = base64LevertData(),
            samtykkeToken =  samtykke,
            foresporsel = "Foresporsel",
            leverandor = "lever"
        )
    }

    fun mockLoggMelding(ident: String = "11886512234", mottaker: String = "938908909", tema: String = "PEN", samtykke: String? = null): LoggMelding {
        val tidspunkt  = LocalDateTime.of(2021, 10, 9, 10, 10)
        return LoggMelding(
            id = null,
            person = ident,
            mottaker = mottaker,
            tema = tema,
            behandlingsGrunnlag = "Lovhjemmel samordningsloven § 27 (samordningsloven paragraf 27)",
            uthentingsTidspunkt = tidspunkt,
            leverteData = base64LevertData(),
            samtykkeToken = samtykke,
            leverandoer = null,
            dataForespoersel = null
        )
    }

    fun mockLoggMeldingAsJson(ident: String = "11886512234", mottaker: String = "938908909", tema: String = "PEN", samtykkeToken: String? = null) =
        mapAnyToJson(mockLoggMelding(ident, mottaker, tema, samtykke = samtykkeToken))


    fun mockNoneValidLoggMeldingJson() : String {
        return """
            {
              "title": "Brown Perfume",
              "description": "Royal_Mirage Sport Brown Perfu",
              "price": 40,
              "discountPercentage": 15.66,
              "rating": 4,
              "stock": 52
            }
        """.trimIndent()
    }

    fun mockNoneValidAAPMelding(): String {
        return """
         {
           "person" : "17468742829",
           "mottaker" : "938908909",
           "tema" : "AAP",
           "behandlingsGrunnlag" : "Lovhjemmel samordningsloven § 27 (samordningsloven paragraf 27)",
           "uthentingsTidspunkt" : "2018-10-19T12:24:21.675",
           "leverteData" : "${base64LevertData()}",
           "leverandoer" : "123213123",
           "saksId" : "4335134"
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

