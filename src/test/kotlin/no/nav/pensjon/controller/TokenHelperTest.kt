package no.nav.pensjon.controller

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.web.server.ResponseStatusException

internal class TokenHelperTest {


    @Nested
    @DisplayName("Check for tokens")
    inner class ValidTokens {

        private val encodedToken = javaClass.getResource("/mockEncodedToken.txt")!!.readText()
        private val encodedSrvToken = javaClass.getResource("/mockEncodedSrvUsrToken.txt")!!.readText()
        private val jwt = JwtToken(encodedToken)
        private val jwtsrv = JwtToken(encodedSrvToken)
        private val context = TokenValidationContext(mapOf("tokendings" to jwt, "difi" to jwt, "servicebruker" to jwtsrv))
        private val tokenContext = TokenContext(context)
        private val tokenhelper = TokenHelper(tokenContext)

        @Test
        fun getPidFromTokenX() {
            Assertions.assertEquals("12345678901", tokenhelper.getPidFromToken())
        }

        @Test
        fun getPid() {
            Assertions.assertEquals("12345678901", tokenhelper.getPid())
        }

        @Test
        fun getSystemUser() {
            Assertions.assertEquals ("srvsporingslogg", tokenhelper.getSystemUserId())
        }

    }


    @Nested
    @DisplayName("No valid tokens")
    inner class NotValidTokens {

        private val encodedToken = javaClass.getResource("/mockEncodedToken.txt")!!.readText()
        private val jwt = JwtToken(encodedToken)
        private val context = TokenValidationContext(mapOf("tokend" to jwt, "Syserr" to jwt))
        private val tokenContext = TokenContext(context)
        private val tokenhelper = TokenHelper(tokenContext)

        @Test
        fun getPidFromTokenX() {
            assertThrows<ResponseStatusException> {
                tokenhelper.getPidFromToken()
            }
        }

        @Test
        fun getPid() {
            assertThrows<ResponseStatusException> {
                tokenhelper.getPid()
            }
        }

        @Test
        fun getSystemUserThrowError() {
            assertThrows<ResponseStatusException> {
                tokenhelper.getSystemUserId()
            }
        }


    }



    private class TokenContext(private val context: TokenValidationContext): TokenValidationContextHolder {
        override fun getTokenValidationContext(): TokenValidationContext {
            return context
        }

        override fun setTokenValidationContext(tokenValidationContext: TokenValidationContext?) {
            //not in use
        }
    }



}