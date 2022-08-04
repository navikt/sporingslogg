package no.nav.pensjon.controller

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class TokenHelper(private val tokenValidationContextHolder: TokenValidationContextHolder) {

    private val log = LoggerFactory.getLogger(javaClass)

    // se appliation.yam #no.nav.security.jwt. under issuer.xxxx
    private enum class Issuer {
        DIFI,
        SERVICEBRUKER,
        TOKENDINGS;
        fun lowercase(): String = this.name.lowercase()
    }

    private fun getClaims(issuer: Issuer): JwtToken {
        val context = tokenValidationContextHolder.tokenValidationContext
        if(context.issuers.isEmpty())
            throw RuntimeException("No issuer found in context")

        val optinalIssuer = context.getJwtTokenAsOptional(issuer.lowercase())

        return if (optinalIssuer.isPresent) {
            optinalIssuer.get()
        } else {
            log.error("No valid token found for issuer: $issuer")
            throw ResponseStatusException(HttpStatus.NOT_FOUND ,"No valid token found")
       }
    }

    /**
     * delevis lånt fra https://github.com/navikt/pam-samtykke-api
     */
    private fun extractForTokendingsIssuer(issuer: Issuer = Issuer.TOKENDINGS): String {
        val token = tokenValidationContextHolder.tokenValidationContext.getJwtToken(issuer.lowercase()).tokenAsString
        log.debug("token: $token")

        tokenValidationContextHolder.tokenValidationContext.getClaims(issuer.lowercase())?.get("pid")?.toString()?.let {
            return it
        } ?: run {
            //Fallback to subject claim, which will typically be used in tests, etc.
            tokenValidationContextHolder.tokenValidationContext.getClaims(issuer.lowercase())?.subject?.toString()?.let {
                return it
            } ?: run {
                log.error("No valid token found for issuer: tokendings")
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "No valid token found")
            }
        }
    }

    fun getPid(): String = getClaims(Issuer.DIFI).jwtTokenClaims.get("pid").toString()

    fun getSystemUserId(): String = getClaims(Issuer.SERVICEBRUKER).subject

    fun getPidFromToken(): String = extractForTokendingsIssuer()

}