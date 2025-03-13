package no.nav.pensjon.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import no.nav.pensjon.util.fromJson2Any
import no.nav.pensjon.util.scrable
import no.nav.pensjon.util.typeRefs
import java.time.LocalDateTime
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
data class LoggMelding(
    val id: String?,
    val person: String?,
    val mottaker: String?,
    val tema: String?,
    val behandlingsGrunnlag: String?,
    val uthentingsTidspunkt: LocalDateTime?,
    val leverteData: String?,
    val samtykkeToken: String?,
    val dataForespoersel: String?,
    val leverandoer: String?
) {

    fun scramblePerson() = person.scrable()

    override fun toString(): String {
        return "LoggMelding [person=${person.scrable()}, mottaker=$mottaker, tema=$tema, behandlingsGrunnlag=$behandlingsGrunnlag, " +
                "uthentingsTidspunkt=$uthentingsTidspunkt, leverteData=$leverteData, samtykkeToken=$samtykkeToken, dataForespoersel=$dataForespoersel, " +
                "leverandoer=$leverandoer]"
    }

    companion object {
        private fun base64Encode(loggMelding: LoggMelding) : LoggMelding = loggMelding.copy(leverteData = Base64.getEncoder().encodeToString(loggMelding.leverteData!!.toByteArray()))

        fun checkForAndEncode(loggMelding: LoggMelding): LoggMelding {
            return if (checkForEncode(loggMelding)) {
                loggMelding // request LoggMelding er alt base64
            } else {
                base64Encode(loggMelding) // data ikke base64 encode data
            }
        }
        fun checkForEncode(loggMelding: LoggMelding): Boolean {
            val pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$"
            val r: Pattern = Pattern.compile(pattern)
            val m: Matcher = r.matcher(loggMelding.leverteData!!)
            return m.find()
        }

        fun fromJson(json: String): LoggMelding = fromJson2Any(json, typeRefs())
        fun fromJsonSkipFail(json: String): LoggMelding = fromJson2Any(json, typeRefs(), false)

        fun fromLoggInnslag(loggInnslag: LoggInnslag): LoggMelding {
            return LoggMelding(
                id = loggInnslag.id?.toString(),
                person = loggInnslag.person,
                mottaker = loggInnslag.mottaker,
                tema = loggInnslag.tema,
                behandlingsGrunnlag = loggInnslag.hjemmel,
                uthentingsTidspunkt = loggInnslag.uthentingsTidspunkt,
                leverteData = loggInnslag.leverteData,
                samtykkeToken = loggInnslag.samtykkeToken,
                dataForespoersel = loggInnslag.foresporsel,
                leverandoer = loggInnslag.leverandor
            )
        }

    }
}

