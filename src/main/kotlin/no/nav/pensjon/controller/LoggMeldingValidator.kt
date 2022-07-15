package no.nav.pensjon.controller

import no.nav.pensjon.domain.LoggMelding
import java.time.LocalDateTime

object LoggMeldingValidator {

    private val ONLY_DIGITS: Regex = "^[0-9]*\$".toRegex()
    private const val PERSON_NR_LENGTH: Int = 11
    private const val MOTTAKER_NR_LENGTH: Int = 9
    private const val TEMA_LENGTH: Int = 3

    private const val BEHANDLINGSGRUNNLAG_MAX_LENGTH: Int = 100
    private const val SAMTYKKE_TOKEN_MAX_LENGTH: Int = 1000

    fun validateRequest(sporingsloggRequest: LoggMelding) {
        personNrValidation(sporingsloggRequest)
        mottakerValidation(sporingsloggRequest)
        temaValidation(sporingsloggRequest)
        behandlingsGrunnlagValidation(sporingsloggRequest)
        levertDataValidation(sporingsloggRequest)
        uthentingsTidspunktValidation(sporingsloggRequest.uthentingsTidspunkt)
        samtykkeTokenValidation(sporingsloggRequest.samtykkeToken)
    }

    private fun personNrValidation(sporingsloggRequest: LoggMelding) {
        throwExceptionIfEmpty(sporingsloggRequest.person, "Person can not be empty")
        throwExceptionIfNotDigits(sporingsloggRequest.person!!, "Person must be a digit")
        if (sporingsloggRequest.person.length != PERSON_NR_LENGTH) throw SporingsloggValidationException("Person must be of length $PERSON_NR_LENGTH")
    }

    private fun mottakerValidation(sporingsloggRequest: LoggMelding) {
        throwExceptionIfEmpty(sporingsloggRequest.mottaker, "Mottaker can not be empty")
        throwExceptionIfNotDigits(sporingsloggRequest.mottaker!!, "Mottaker must be a digit")
        if (sporingsloggRequest.mottaker.length != MOTTAKER_NR_LENGTH) throw SporingsloggValidationException("Mottaker must be of length $MOTTAKER_NR_LENGTH")
    }

    private fun temaValidation(sporingsloggRequest: LoggMelding) {
        throwExceptionIfEmpty(sporingsloggRequest.tema, "Tema can not be empty")
        if (sporingsloggRequest.tema!!.length != TEMA_LENGTH) throw SporingsloggValidationException("Tema must be of length $TEMA_LENGTH")
    }

    private fun behandlingsGrunnlagValidation(sporingsloggRequest: LoggMelding) {
        throwExceptionIfEmpty(sporingsloggRequest.behandlingsGrunnlag, "BehandlingsGrunnlag can not be empty")
        throwExceptionIfBehandlingsGrunnlagIsToLong(sporingsloggRequest.behandlingsGrunnlag!!)
    }

    private fun levertDataValidation(sporingsloggRequest: LoggMelding) {
        throwExceptionIfEmpty(sporingsloggRequest.leverteData, "LeverteData can not be empty")
    }

    private fun uthentingsTidspunktValidation(uthentingsTidspunkt: LocalDateTime?) {
        if (uthentingsTidspunkt == null) throw SporingsloggValidationException("""["UthentingsTidspunkt must be ISO-Format without timezone"]""")
    }

    private fun samtykkeTokenValidation(samtykkeToken: String?) {
        if (samtykkeToken != null && samtykkeToken.length > SAMTYKKE_TOKEN_MAX_LENGTH) throw SporingsloggValidationException("""["SamtykkeToken can not be longer than $SAMTYKKE_TOKEN_MAX_LENGTH characters "]""")
    }

    private fun throwExceptionIfEmpty(string: String?, message: String) {
        if (string == null || string.isEmpty()) throw SporingsloggValidationException(message)
    }

    private fun throwExceptionIfNotDigits(numericalString: String, message: String) {
        if (!isDigits(numericalString)) throw SporingsloggValidationException(message)
    }

    private fun throwExceptionIfBehandlingsGrunnlagIsToLong(behandlingsGrunnlag: String) {
        if (behandlingsGrunnlag.length > BEHANDLINGSGRUNNLAG_MAX_LENGTH) throw SporingsloggValidationException("""["BehandlingsGrunnlag can not be longer than $BEHANDLINGSGRUNNLAG_MAX_LENGTH characters "]""")
    }

    private fun isDigits(numericalString: String): Boolean {
        return numericalString.matches(ONLY_DIGITS)
    }

}

internal class SporingsloggValidationException(message: String) : Exception(message)