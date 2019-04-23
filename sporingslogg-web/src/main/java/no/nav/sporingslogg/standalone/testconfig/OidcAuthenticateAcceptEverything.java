package no.nav.sporingslogg.standalone.testconfig;

import java.security.Key;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.sporingslogg.oidc.OidcAuthenticate;

class OidcAuthenticateAcceptEverything extends OidcAuthenticate { // Godtar alle tokens så lenge de kan parses/decodes til JSON

    private final Logger log = LoggerFactory.getLogger(getClass());
    
    public static String USER_TO_REFUSE = null;

    public OidcAuthenticateAcceptEverything() {
		super((Key) null, null, null);
	}

	public String getVerifiedSubject(String bearerToken)  {
		JwtConsumer jwtConsumer = buildJwtConsumer(); 
		try {
			JwtClaims jwtClaims = jwtConsumer.processToClaims(bearerToken);
			log.debug("Dummy OIDC autentiserer bruker: "+jwtClaims.getSubject());
	        if (USER_TO_REFUSE == null || !USER_TO_REFUSE.equalsIgnoreCase(jwtClaims.getSubject())) {
	        	return jwtClaims.getSubject();
	        } else {
				log.debug("Dummy OIDC nekter UNAUTH-bruker: "+jwtClaims.getSubject());
				throw new SecurityException("Authentication failed");
	        }
	        
		} catch (InvalidJwtException e) {
			throw new RuntimeException("OIDC-token er ikke gyldig", e);
			
		} catch (MalformedClaimException e) {
			throw new RuntimeException("Kan ikke hente ut bruker fra OIDC-token", e);
		}
	}

	private JwtConsumer buildJwtConsumer() {
		JwtConsumerBuilder builder = new JwtConsumerBuilder();
		builder.setSkipAllValidators();
		builder.setSkipSignatureVerification();
		return builder.build();
	}
}
