package no.nav.pensjon.config

import no.nav.security.token.support.client.spring.oauth2.EnableOAuth2Client
import no.nav.security.token.support.spring.api.EnableJwtTokenValidation
import org.springframework.context.annotation.Configuration

@Configuration
@EnableJwtTokenValidation
@EnableOAuth2Client(cacheEnabled = true)
class SecurityConfiguration {

}