package no.nav.sporingslogg.oidc;

import java.io.IOException;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.UnresolvableKeyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.sporingslogg.standalone.PropertyNames;
import no.nav.sporingslogg.standalone.PropertyUtil;

public class OidcAuthenticate {

    private final Logger log = LoggerFactory.getLogger(getClass());

    // ID-porten gir ut tokens med "acr":"Level4" hvis innlogging er skjedd med BankID e.l.
	private static final String AUTH_LEVEL_CLAIM = "acr";
    static final String AUTH_LEVEL_4 = "Level4";
    
	// Aksepterer ikke "evigvarende" tokens
	private static final int MAX_VALIDITY_SECONDS = 3600;
	// Godta slingringsmonn pga unøyaktige klokker
	private static final int AKSEPTERT_TIDSFORSKJELL_SEKUNDER = 30;

	private final Key publicKey; // kun til testbruk
	private final String publicIssuer;
	private final Map<String, VerificationKeyResolver> resolverByIssuer = new HashMap<>();
	private final String requiredAuthLevel;
	
	// Brukes bare av test
	public OidcAuthenticate(Key publicKey, String publicIssuer, String requiredAuthLevel) {
		this.publicKey = publicKey;
		this.publicIssuer = publicIssuer;
		this.requiredAuthLevel = requiredAuthLevel;
		log.info("OIDC authentication set up with DUMMY key and issuer, authlevel: " + requiredAuthLevel);
	}

	// Brukes bare av test
	public OidcAuthenticate(VerificationKeyResolver keyResolver, String issuer) {
		this.publicKey = null;
		this.publicIssuer = null;
		resolverByIssuer.put(issuer, keyResolver);
		requiredAuthLevel = getRequiredAuthLevel();
		log.info("OIDC authentication set up with DUMMY resolver and issuer, authlevel: " + requiredAuthLevel);
	}

	// Endret til å ta inn lister som kommaseparerte strenger, for å dekke at Miccemus skifter adresser. Greit å tolerere flere issuere uansett.
	public OidcAuthenticate(String issuerListCS, String jwksListCS) {
		this.publicKey = null;
		this.publicIssuer = null;
		String[] issuerList = issuerListCS.split(",");
		String[] jwksList = jwksListCS.split(",");
		int i = 0;
		for (String issuer : issuerList) {
			HttpsJwks jwks = new HttpsJwks(jwksList[i]);
			HttpsJwksVerificationKeyResolver keyResolver = new HttpsJwksVerificationKeyResolver(jwks);
			resolverByIssuer.put(issuer, keyResolver);
			log.info("OIDC authentication set up with issuer and jwks: " + issuer + " " + jwksList[i]);
			i++;
		}
		requiredAuthLevel = getRequiredAuthLevel();
	}

	private String getRequiredAuthLevel() {
        String authLevelProperty = PropertyUtil.getProperty(PropertyNames.PROPERTY_OIDC_AUTHLEVEL);
        if ("0".equals(authLevelProperty)) {
        	// Bruker test-OIDCprovider, som ikke angir authlevel
            log.info("OIDC auth level check turned OFF by env property " + PropertyNames.PROPERTY_OIDC_AUTHLEVEL);
        	return null;
        }
        if ("4".equals(authLevelProperty)) {
        	// Bruker ID-porten
            log.info("Required OIDC auth level set to 4 by env property " + PropertyNames.PROPERTY_OIDC_AUTHLEVEL);
        	return AUTH_LEVEL_4;
        }
        // Godtar egentlig ikke andre settinger, men velger isåfall sikreste variant: krever authlevel 4
        log.warn("Required OIDC auth level is not set, will require level 4");
    	return AUTH_LEVEL_4;
	}

	private final int MISSING_ISSUER = 11;
	private final int FEIL_ISSUER = 12;
	private final int FOER_NOT_BEFORE = 6;
	private final int MANGLER_EXPIRY = 2;
	private final int PASSERT_EXPIRY = 1;
	private final int FEIL_SIGNATUR = 9;
	private final int FEIL_TOKEN_FORMAT = 17;

	public String getVerifiedSubject(String bearerToken)  { 
		String issuer = getUnverifiedIssuer(bearerToken);
		JwtConsumer jwtConsumer = buildJwtConsumer(issuer); 
		log.info("requiredAuthLevel : " + requiredAuthLevel);
		log.debug("Bearer Token: " + bearerToken);

		try {
			JwtClaims jwtClaims = jwtConsumer.processToClaims(bearerToken);

			if (requiredAuthLevel != null) {
				String authLevelClaim = jwtClaims.getStringClaimValue(AUTH_LEVEL_CLAIM);
				if (!requiredAuthLevel.equals(authLevelClaim)) {
					throw new RuntimeException("OIDC-token har ikke tilstrekkelig autentiseringsnivå ("+requiredAuthLevel+"): "+authLevelClaim);
				}
			}

			//return jwtClaims.getSubject();
			if (jwtClaims.hasClaim("pid")) {
				log.info("Henter pid fra token");
				return jwtClaims.getClaimValue("pid").toString();
			} else if(jwtClaims.hasClaim("sub")) {
				log.info("Henter sub fra token");
				return jwtClaims.getSubject();
			} else {
				log.warn("Token mangler pid");
				throw new RuntimeException("OIDC-token mangler pid");
			}

		} catch (InvalidJwtException e) {
			if (e.hasErrorCode(MISSING_ISSUER)) {
				throw new RuntimeException("OIDC-token er ikke gyldig, mangler issuer", e);
			}
			if (e.hasErrorCode(FEIL_ISSUER)) {
				throw new RuntimeException("OIDC-token er ikke gyldig, er ikke utgitt av " + issuer, e);
			}
			if (e.hasErrorCode(FOER_NOT_BEFORE)) {
				throw new RuntimeException("OIDC-token er ennå ikke gyldig, 'not before' er i framtiden", e);
			}
			if (e.hasErrorCode(MANGLER_EXPIRY)) {
				throw new RuntimeException("OIDC-token er ikke gyldig, har ingen gyldighetsgrense", e);
			}
			if (e.hasErrorCode(PASSERT_EXPIRY)) {
				throw new RuntimeException("OIDC-token er ikke gyldig, gyldighetstid utløpt", e);
			}
			if (e.hasErrorCode(FEIL_SIGNATUR)) {
				throw new RuntimeException("OIDC-token er ikke gyldig, feil signatur", e);
			}
			if (e.hasErrorCode(FEIL_TOKEN_FORMAT)) {
				Throwable cause = e.getCause();
				if (cause != null && cause instanceof UnresolvableKeyException) {
					Throwable causeCause = cause.getCause();
					if (causeCause != null && causeCause instanceof IOException) {
						throw new RuntimeException("Kan ikke kontakte OIDC-provider for å validere OIDC-token", e);
					}
					throw new RuntimeException("OIDC-token inneholder ukjent key-Id", e);
				}
				throw new RuntimeException("OIDC-token er ikke gyldig, feil format", e);
			}
			throw new RuntimeException("OIDC-token er ikke gyldig", e);
			
		} catch (MalformedClaimException e) {
			throw new RuntimeException("Kan ikke hente ut bruker fra OIDC-token", e);
		}
	}

	public String getUnverifiedSubject(String bearerToken)  {  // Denne er laget for å bypasse all token-sjekking i test - ikke bruk den i prod
		JwtConsumer jwtConsumer = buildAcceptAnythingJwtConsumer(); 
		try {
			JwtClaims jwtClaims = jwtConsumer.processToClaims(bearerToken);
	        return jwtClaims.getSubject();
	        
		} catch (InvalidJwtException e) {
			throw new RuntimeException("OIDC-token er ikke gyldig", e);
			
		} catch (MalformedClaimException e) {
			throw new RuntimeException("Kan ikke hente ut bruker fra OIDC-token", e);
		}
	}
	
	private String getUnverifiedIssuer(String bearerToken)  {  
		JwtConsumer jwtConsumer = buildAcceptAnythingJwtConsumer();
		try {
			JwtClaims jwtClaims = jwtConsumer.processToClaims(bearerToken);
	        return jwtClaims.getIssuer();
	        
		} catch (InvalidJwtException e) {
			throw new RuntimeException("OIDC-token er ikke gyldig", e);
			
		} catch (MalformedClaimException e) {
			throw new RuntimeException("Kan ikke hente ut issuer fra OIDC-token", e);
		}
	}
	
	private JwtConsumer buildJwtConsumer(String issuer) {
		JwtConsumerBuilder builder = new JwtConsumerBuilder()
	            .setRequireExpirationTime() 
	            .setMaxFutureValidityInMinutes(MAX_VALIDITY_SECONDS) 
	            .setAllowedClockSkewInSeconds(AKSEPTERT_TIDSFORSKJELL_SEKUNDER) 
	            .setSkipDefaultAudienceValidation();
		
		if (publicKey != null) {
            builder.setVerificationKey(publicKey);
            builder.setExpectedIssuer(publicIssuer);
		} else {
			VerificationKeyResolver keyResolver = resolverByIssuer.get(issuer);
			if (keyResolver == null) {
				throw new RuntimeException("OIDC-token inneholder ukjent issuer: " + issuer);
			}
            builder.setExpectedIssuer(issuer);
			builder.setVerificationKeyResolver(keyResolver);
		}
		
		return builder.build();
	}
	private JwtConsumer buildAcceptAnythingJwtConsumer() {
		JwtConsumerBuilder builder = new JwtConsumerBuilder()
	            .setSkipAllValidators()
	            .setSkipSignatureVerification();
		return builder.build();
	}
}
