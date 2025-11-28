package no.nav.pensjon.controller

import no.nav.security.token.support.core.context.TokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class TokenHelper(private val tokenValidationContextHolder: TokenValidationContextHolder) {

    private val log = LoggerFactory.getLogger(javaClass)

    // se appliation.yam #no.nav.security.jwt. under issuer.xxxx
    enum class Issuer {
        DIFI,
        SERVICEBRUKER,
        TOKENDINGS,
        ENTRAID;
        fun lowercase(): String = this.name.lowercase()
    }

    private fun getClaims(issuer: Issuer, warningOf: Boolean = false): String {
        val context = tokenValidationContextHolder.getTokenValidationContext()
        if(context.issuers.isEmpty())
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No issuer found in context")

        val optinalIssuer = context.getJwtTokenAsOptional(issuer.lowercase())

        val jwtToken = if (optinalIssuer.isPresent) {
            optinalIssuer.get()
        } else {
            if (!warningOf)
                log.error("No valid token found for issuer: $issuer")

            throw ResponseStatusException(HttpStatus.NOT_FOUND ,"No valid token found")
       }

       return if (issuer == Issuer.DIFI) {
           //pid is bruker/fnr fra difi
           jwtToken.jwtTokenClaims.get("pid").toString()
       } else {
           //subject is systembruker
           jwtToken.subject
       }
    }

    /**
     * delevis lånt fra https://github.com/navikt/pam-samtykke-api
     */
    private fun extractForTokendingsIssuer(issuer: Issuer = Issuer.TOKENDINGS): String {
        val context = tokenValidationContextHolder.getTokenValidationContext()

        try {
            val tokenclaims = context.getClaims(issuer.lowercase())
            return tokenclaims.get("pid").toString()

        } catch (ex: IllegalArgumentException) {
            log.warn("faild to find pid on $issuer", ex)
            try {
                val tokenclaims = context.getClaims(issuer.lowercase())
                return tokenclaims.subject
            } catch (ex: IllegalArgumentException) {
                log.error("No valid token found for issuer: tokendings")
                throw ResponseStatusException(HttpStatus.NOT_FOUND, "No valid token found")
            }
        }
    }

//    private fun extractForTokendingsIssuerOLD(issuer: Issuer = Issuer.TOKENDINGS): String {
//        tokenValidationContextHolder.getTokenValidationContext().getClaims(issuer.lowercase()).get("pid")?.toString()?.let {
//            return it
//        } ?: run {
//            //Fallback to subject claim, which will typically be used in tests, etc.
//            tokenValidationContextHolder.getTokenValidationContext().getClaims(issuer.lowercase()).subject?.toString()?.let {
//                return it
//            } ?: run {
//                log.error("No valid token found for issuer: $issuer")
//                throw ResponseStatusException(HttpStatus.NOT_FOUND, "No valid token found")
//            }
//        }
//    }


    fun getPid(): String = getClaims(Issuer.DIFI)

    fun getSystemUserId(): String = getClaims(Issuer.SERVICEBRUKER)

    fun getEntraId(): String = getClaims(Issuer.ENTRAID)

    fun getPidFromToken(): String = extractForTokendingsIssuer()

    fun getSystemUserOrEntraId(): String = try { getClaims(Issuer.SERVICEBRUKER, true)
                                                  } catch (e: Exception) {
                                                    getEntraId()
                                                  }
}