package no.nav.pensjon.controller

import no.nav.pensjon.TestHelper.mockLoggMelding
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class LoggMeldingValidatorTest {


    @Nested
    @DisplayName("Validate fnr/pid")
    inner class ValidatePid {

        @Test
        fun `validate on valid fnr or pid`() {
            LoggMeldingValidator.validateRequest(mockLoggMelding())
        }

        @Test
        fun `validate on fnr or pid is null`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(person = null))
            }
        }

        @Test
        fun `validate on fnr or pid too short`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding("12890211"))
            }
        }

        @Test
        fun `validate on fnr or pid too long`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding("12890211345134541"))
            }
        }

    }

    @Nested
    @DisplayName("Validate mottaker")
    inner class ValidateMottaker {

        @Test
        fun `validate on mottaker is valid`() {
            LoggMeldingValidator.validateRequest(mockLoggMelding())
        }

        @Test
        fun `validate on mottaker is null`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(mottaker = null))
            }
        }

        @Test
        fun `validate on mottaker is text`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(mottaker = "ABBA SONG"))
            }
        }

        @Test
        fun `validate on mottaker is too short`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(mottaker = "1233"))
            }
        }

        @Test
        fun `validate on mottaker is too long`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(mottaker = "13423424562452456245"))
            }
        }

    }

    @Nested
    @DisplayName("Validate tema")
    inner class ValidateTema {

        @Test
        fun `validate on tema is valid`() {
            LoggMeldingValidator.validateRequest(mockLoggMelding())
        }

        @Test
        fun `validate on tema is null`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(tema = null))
            }
        }

        @Test
        fun `validate on tema is too short`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest( mockLoggMelding().copy(tema = "PE"))
            }
        }

        @Test
        fun `validate on tema is too long`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest( mockLoggMelding().copy(mottaker = "PENSJON"))
            }
        }

    }

    @Nested
    @DisplayName("Validate behandlingsGrunnlag")
    inner class ValidateBehandlingsGrunnlag {

        @Test
        fun `validate on behandlingsGrunnlag is valid`() {
            LoggMeldingValidator.validateRequest(mockLoggMelding())
        }

        @Test
        fun `validate on behandlingsGrunnlag is null`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(behandlingsGrunnlag = null))
            }
        }

        @Test
        fun `validate on behandlingsGrunnlag is too long`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest( mockLoggMelding().copy(behandlingsGrunnlag = "behandlingsGrunnlag ".repeat(10) ))
            }
        }

    }

    @Nested
    @DisplayName("Validate levertData")
    inner class ValidateLevertData {

        @Test
        fun `validate on behandlingsGrunnlag is valid`() {
            LoggMeldingValidator.validateRequest(mockLoggMelding())
        }

        @Test
        fun `validate on behandlingsGrunnlag is null`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(behandlingsGrunnlag = null))
            }
        }

    }

    @Nested
    @DisplayName("Validate tidspunkt")
    inner class ValidateTidspunkt {

        @Test
        fun `validate on tidspunkt is valid`() {
            LoggMeldingValidator.validateRequest(mockLoggMelding())
        }

        @Test
        fun `validate on tidspunkt is null`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(uthentingsTidspunkt = null))
            }
        }

    }

    @Nested
    @DisplayName("Validate samtykkeToken")
    inner class ValidateSamtykkeToken {

        @Test
        fun `validate on samtykkeToken is valid`() {
            LoggMeldingValidator.validateRequest(mockLoggMelding())
        }

        @Test
        fun `validate on samtykkeToken is too long`() {
            assertThrows<SporingsloggValidationException> {
                LoggMeldingValidator.validateRequest(mockLoggMelding().copy(samtykkeToken = "samtykkeToken".repeat(3000)))
            }
        }

    }

}