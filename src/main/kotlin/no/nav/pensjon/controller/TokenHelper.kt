package no.nav.pensjon.controller

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import no.nav.security.token.support.core.jwt.JwtToken
import org.springframework.stereotype.Component

@Component
class TokenHelper(private val tokenValidationContextHolder: TokenValidationContextHolder) {

    enum class Issuer {
        difi,
        servicebruker;
    }

    private fun getClaims(issuer: Issuer): JwtToken {
        val context = tokenValidationContextHolder.tokenValidationContext
        if(context.issuers.isEmpty())
            throw RuntimeException("No issuer found in context")

        val optinalIssuer = context.getJwtTokenAsOptional(issuer.name)

        return if (optinalIssuer.isPresent) {
            optinalIssuer.get()
        } else {
            throw RuntimeException("No valid token found for $issuer in context")
       }
    }

    fun getPid() = getClaims(Issuer.difi).jwtTokenClaims.get("pid").toString()

    fun getSystemUserId() = getClaims(Issuer.servicebruker).subject


}