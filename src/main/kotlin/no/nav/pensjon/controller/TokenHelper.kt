package no.nav.pensjon.controller

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class TokenHelper(private val tokenValidationContextHolder: TokenValidationContextHolder) {

    // se appliation.yam #no.nav.security.jwt. under issuer.xxxx
    private enum class Issuer {
        DIFI,
        SERVICEBRUKER;
    }


    private fun getClaims(issuer: Issuer): JwtToken {
        val context = tokenValidationContextHolder.tokenValidationContext
        if(context.issuers.isEmpty())
            throw RuntimeException("No issuer found in context")

        val optinalIssuer = context.getJwtTokenAsOptional(issuer.name.lowercase())

        return if (optinalIssuer.isPresent) {
            optinalIssuer.get()
        } else {
            throw ResponseStatusException(HttpStatus.NOT_FOUND ,"No valid token found for $issuer in context")
       }
    }

    fun getPid(): String = getClaims(Issuer.DIFI).jwtTokenClaims.get("pid").toString() ?: ""

    fun getSystemUserId(): String = getClaims(Issuer.SERVICEBRUKER).subject


}