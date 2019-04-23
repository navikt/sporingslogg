package no.nav.sporingslogg.oidc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.security.Key;
import java.util.Base64;
import java.util.List;

import org.jose4j.jwk.HttpsJwks;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.jwx.JsonWebStructure;
import org.jose4j.keys.resolvers.HttpsJwksVerificationKeyResolver;
import org.jose4j.keys.resolvers.VerificationKeyResolver;
import org.jose4j.lang.UnresolvableKeyException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class VerifyOidcTest {

	private static final String USER = "meg";
	private static final String ISSUER = "issueren";
	private static final String PUBLIC_KEY = "hemmelig_og_lenger_enn_64_bytes_for_sure";
	private static final String ANNEN_KEY = "en_annen_key_lenger_enn_64_bytes_for_sure";
	
	private OidcAuthenticate oidcAuthenticate;
	
	@Before
	public void setup() {
		oidcAuthenticate = new OidcAuthenticate(OidcTokenGeneratorUtil.getKey(PUBLIC_KEY), ISSUER, OidcAuthenticate.AUTH_LEVEL_4);
	}
	
	@Test
	public void okTokenErOk() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		String user = oidcAuthenticate.getVerifiedSubject(token);
		assertEquals(USER, user);
	}
	
	@Test
	public void okTokenMedKeyResolverErOk() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		oidcAuthenticate = new OidcAuthenticate(getOkResolver(), ISSUER);
		String user = oidcAuthenticate.getVerifiedSubject(token);
		assertEquals(USER, user);
	}
	
	@Test
	public void feilTokenInnholdGirFeilkode() throws Exception {
		try {
			oidcAuthenticate.getVerifiedSubject("tull_og_tøys");
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer feil format", e.getMessage().contains("feil format"));
		}
	}

	@Test
	public void manglendeIssuerGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		claims.setIssuer(null);
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer missing issuer", e.getMessage().contains("mangler issuer"));
		}
	}

	@Test
	public void feilIssuerGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		claims.setIssuer("feil");
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer missing issuer", e.getMessage().contains("ikke utgitt av"));
		}
	}

	@Test
	public void framtidigNotBeforeGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		claims.setNotBeforeMinutesInThePast(-5);
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer not before i framtiden", e.getMessage().contains("not before"));
		}
	}

	@Test
	public void manglendeExpiryGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		claims.setExpirationTime(null);
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer null expiry", e.getMessage().contains("gyldighetsgrense"));
		}
	}

	@Test
	public void expiryPassertGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		claims.setExpirationTimeMinutesInTheFuture(-1);
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer passert expiry", e.getMessage().contains("gyldighetstid"));
		}
	}

	@Test
	public void feilSignaturGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		String token = OidcTokenGeneratorUtil.generateToken(claims, OidcTokenGeneratorUtil.getKey(ANNEN_KEY));
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer feil signatur", e.getMessage().contains("signatur"));
		}
	}

	@Test
	public void utgaattKeyGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		oidcAuthenticate = new OidcAuthenticate(getGammelResolver(), ISSUER);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer utgått key", e.getMessage().contains("ukjent key-Id"));
		}
	}

	@Test
	public void connectionProblemerTilOidcProviderGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		oidcAuthenticate = new OidcAuthenticate(getConnectionExceptionResolver(), ISSUER);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer IO exception", e.getMessage().contains("ikke kontakte OIDC-provider"));
		}
	}

	@Test
	public void authLevelUnder4GirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		claims.setStringClaim("acr", "Level3");
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer for lavt aut.nivå", e.getMessage().contains("autentiseringsnivå"));
		}
	}

	@Test
	public void ingenAuthLevelGirFeilkode() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		claims.unsetClaim("acr");
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		try {
			oidcAuthenticate.getVerifiedSubject(token);
			fail("fikk ikke forventet exception");
		} catch (Exception e) {
			assertTrue("Forventer for lavt aut.nivå", e.getMessage().contains("autentiseringsnivå"));
		}
	}

	@Test
	public void ingenAuthLevelOkHvisSjekkErSlaattAv() throws Exception {
		JwtClaims claims = OidcTokenGeneratorUtil.generateClaims(USER, ISSUER);
		claims.unsetClaim("acr");
		String token = OidcTokenGeneratorUtil.generateToken(claims, PUBLIC_KEY);
		oidcAuthenticate = new OidcAuthenticate(OidcTokenGeneratorUtil.getKey(PUBLIC_KEY), ISSUER, null);
		oidcAuthenticate.getVerifiedSubject(token);
	}

	private VerificationKeyResolver getOkResolver() {
		return new VerificationKeyResolver() {			
			@Override
			public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
				return OidcTokenGeneratorUtil.getKey(PUBLIC_KEY);
			}
		};
	}

	private VerificationKeyResolver getGammelResolver() {
		return new VerificationKeyResolver() {			
			@Override
			public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
				throw new UnresolvableKeyException("FInner ikke key, den er for gammel");
			}
		};
	}

	private VerificationKeyResolver getConnectionExceptionResolver() {
		return new VerificationKeyResolver() {			
			@Override
			public Key resolveKey(JsonWebSignature jws, List<JsonWebStructure> nestingContext) throws UnresolvableKeyException {
				throw new UnresolvableKeyException("Kan ikke kontakte provider", new IOException("internett er lagt ned"));
			}
		};
	}

	@Test
	public void parseSampleContentsWorks() throws Exception {
		// Hentet fra testmiljø
//		String jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImZ5akpfczQwN1ZqdnRzT0NZcEItRy1IUTZpYzJUeDNmXy1JT3ZqVEFqLXcifQ.eyJleHAiOjE1NDAzNzE3NzgsIm5iZiI6MTU0MDM2ODE3OCwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5taWNyb3NvZnRvbmxpbmUuY29tL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMi92Mi4wLyIsInN1YiI6IjEyNDU2Nzg5MTAiLCJhdWQiOiIwMDkwYjZlMS1mZmNjLTRjMzctYmMyMS0wNDlmN2QxZjBmZTUiLCJhY3IiOiJMZXZlbDQiLCJub25jZSI6Imgtalp0dURGYW5HWDVuSWp4WTNYT2pMUjRsM05zY09vbXB1c2NQOXZnX0UiLCJpYXQiOjE1NDAzNjgxNzgsImF1dGhfdGltZSI6MTU0MDM2ODE3OCwianRpIjoiSGFsdmFyZC5IZWxsYS5LdmVybmFAbmF2Lm5vOjFlOTY0YmMxLTQ5MWYtNDJlNy1iYzY5LWY0YmNlNGQxYjZkMyIsImF0X2hhc2giOiJhM1FFREdDRTJkUjhvOTlvNDNZeTFBIn0.RW4ObubMa7w2x0UR-0sf3-RYx0qfuCdcsKh1X--mvg5FQyluCBsseXFl_w1InUJA4zkr2Ww7FbkAzNloaOi51oclc0VZ23fk4QWcH-Wu3m8ClRf4vkUTYgLhgQAilctjkQdmTVw_9xQotdeMZ8fEyklSEVWpY1VG9kv6cGNF71zXkkrrbo-NRITzXO2-E4q5N4NmBG5DY0DWymQFYEA-ajy1N6ijadLKb8Tx7eyT0_RbA97B2OXJJmige8we1XeGWPGjP84LppyNAxXzg3v39s3bzhZ-r6xmNAzpZkBI7xCXmOue_g0UzB_gr-dDP-U7f66d311Xl665mKGeG-rngg";
		String jwt = "eyJraWQiOiIzNWRhYTFkNS0xMDY0LTRkZTAtODc3OC03M2JhNWU3YTZkNGYiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZBQkFDUEVQIiwiYXVkIjpbInNydkFCQUNQRVAiLCJwcmVwcm9kLmxvY2FsIl0sInZlciI6IjEuMCIsIm5iZiI6MTU0MjI4NTc4MywiYXpwIjoic3J2QUJBQ1BFUCIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1NDIyODU3ODMsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLm5haXMucHJlcHJvZC5sb2NhbCIsImV4cCI6MTU0MjI4OTM4MywiaWF0IjoxNTQyMjg1NzgzLCJqdGkiOiJlZTFiMGJmMC0zYzNlLTQzNGQtOGQwOS03MTI1NTc1NWZmZmEifQ.OJc5KUSlOsSbuyJlu5-KV5mFk5349NeCxqwqYa494tVoG6ZWI-8K2-EEziWhvUGllO9entlwPaQpUk_xRQkRR4Cqp9ymhTSGUebuV4m8sKdr04IP8sPead2qOJSwN9Zpv64YGkGnb9feuCUXmPvhRCN49iNEB28ED9OhDaOr65suKo0FF4lzdZG91ehsOpuTYKxz8uSCUV08-8tflnY1BRcQoowdScHP_L2Su_N-dDuzV2tQ3CrLmeTZmo70T4c8UVOX1_HRvkv9tCmD0P85c1ZNkNKP2Q-oHPK7XQOqCGPVbt9jfUoSzhBySyw4joKBYn6cUkLYGJLAENQL6uF8jQ";
		// Kan ikke validere - har ikke key og tiden har gått ut...
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
				   .setSkipSignatureVerification()
				   .setSkipAllDefaultValidators()
		           .build(); 
		// ...men det skal gå an å pakke ut tokenet
		JwtClaims claims = jwtConsumer.processToClaims(jwt);
//		System.out.println(claims);
		String header = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImZ5akpfczQwN1ZqdnRzT0NZcEItRy1IUTZpYzJUeDNmXy1JT3ZqVEFqLXcifQ";
		byte[] decode = Base64.getDecoder().decode(header);
		System.out.println("Header: " + new String(decode));
	}
	
	// SKal bare kjøres manuelt for å teste kontakt med Azure.
	// Gir exception fordi key er gammel, men sjekk at man får tilbake keys
	@Ignore
	@Test
	public void manualTestWithAzureResolver() throws Exception {
		// Funnet i miljøvariable, uten disse blir det "connection refused"
		System.setProperty("https.proxyHost", "webproxy-utvikler.nav.no");
		System.setProperty("https.proxyPort", "8088");
	    HttpsJwks httpsJkws = new HttpsJwks("https://login.microsoftonline.com/common/discovery/keys");
	    HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
	            .setRequireExpirationTime() 
	            .setMaxFutureValidityInMinutes(500) 
	            .setAllowedClockSkewInSeconds(30) 
	            .setSkipDefaultAudienceValidation()
	            .setExpectedIssuer("iss") 
	            .setVerificationKeyResolver(httpsJwksKeyResolver)
	            .build(); 
		String jwt = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImZ5akpfczQwN1ZqdnRzT0NZcEItRy1IUTZpYzJUeDNmXy1JT3ZqVEFqLXcifQ.eyJleHAiOjE1NDAzNzE3NzgsIm5iZiI6MTU0MDM2ODE3OCwidmVyIjoiMS4wIiwiaXNzIjoiaHR0cHM6Ly9sb2dpbi5taWNyb3NvZnRvbmxpbmUuY29tL2QzOGYyNWFhLWVhYjgtNGM1MC05ZjI4LWViZjkyYzEyNTZmMi92Mi4wLyIsInN1YiI6IjEyNDU2Nzg5MTAiLCJhdWQiOiIwMDkwYjZlMS1mZmNjLTRjMzctYmMyMS0wNDlmN2QxZjBmZTUiLCJhY3IiOiJMZXZlbDQiLCJub25jZSI6Imgtalp0dURGYW5HWDVuSWp4WTNYT2pMUjRsM05zY09vbXB1c2NQOXZnX0UiLCJpYXQiOjE1NDAzNjgxNzgsImF1dGhfdGltZSI6MTU0MDM2ODE3OCwianRpIjoiSGFsdmFyZC5IZWxsYS5LdmVybmFAbmF2Lm5vOjFlOTY0YmMxLTQ5MWYtNDJlNy1iYzY5LWY0YmNlNGQxYjZkMyIsImF0X2hhc2giOiJhM1FFREdDRTJkUjhvOTlvNDNZeTFBIn0.RW4ObubMa7w2x0UR-0sf3-RYx0qfuCdcsKh1X--mvg5FQyluCBsseXFl_w1InUJA4zkr2Ww7FbkAzNloaOi51oclc0VZ23fk4QWcH-Wu3m8ClRf4vkUTYgLhgQAilctjkQdmTVw_9xQotdeMZ8fEyklSEVWpY1VG9kv6cGNF71zXkkrrbo-NRITzXO2-E4q5N4NmBG5DY0DWymQFYEA-ajy1N6ijadLKb8Tx7eyT0_RbA97B2OXJJmige8we1XeGWPGjP84LppyNAxXzg3v39s3bzhZ-r6xmNAzpZkBI7xCXmOue_g0UzB_gr-dDP-U7f66d311Xl665mKGeG-rngg";
		JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
		System.out.println(jwtClaims.getSubject());
	}

	// SKal bare kjøres manuelt for å teste kontakt med rest-STS i nais preprod.
	// Gir exception fordi key er gammel, men kan hente nytt token fra https://security-token-service.nais.preprod.local/rest/v1/sts/token?grant_type=client_credentials&scope=openid
	@Ignore
	@Test
	public void manualTestWithRestStsResolver() throws Exception {
		SslCertificateBypasser.disableSSLCertificateChecking(); // For å få til SSL
	    HttpsJwks httpsJkws = new HttpsJwks("https://security-token-service-t10.nais.preprod.local/rest/v1/sts/jwks");
	    HttpsJwksVerificationKeyResolver httpsJwksKeyResolver = new HttpsJwksVerificationKeyResolver(httpsJkws);
		JwtConsumer jwtConsumer = new JwtConsumerBuilder()
	            .setRequireExpirationTime() 
	            .setMaxFutureValidityInMinutes(500) 
	            .setAllowedClockSkewInSeconds(30) 
	            .setExpectedIssuer("https://security-token-service-t10.nais.preprod.local") 
	            .setSkipDefaultAudienceValidation()
	            .setVerificationKeyResolver(httpsJwksKeyResolver)
	            .build(); 
		String jwt = "eyJraWQiOiI4M2ZhNTgyZC0zYWM2LTQzZWEtYWRlNi1hY2MyYjJmMjU2MWIiLCJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJzcnZBQkFDUEVQIiwiYXVkIjpbInNydkFCQUNQRVAiLCJwcmVwcm9kLmxvY2FsIl0sInZlciI6IjEuMCIsIm5iZiI6MTU1NjAxMDE0MSwiYXpwIjoic3J2QUJBQ1BFUCIsImlkZW50VHlwZSI6IlN5c3RlbXJlc3N1cnMiLCJhdXRoX3RpbWUiOjE1NTYwMTAxNDEsImlzcyI6Imh0dHBzOlwvXC9zZWN1cml0eS10b2tlbi1zZXJ2aWNlLXQxMC5uYWlzLnByZXByb2QubG9jYWwiLCJleHAiOjE1NTYwMTM3NDEsImlhdCI6MTU1NjAxMDE0MSwianRpIjoiMGM0NGYyNDEtMzJjOS00OTIwLWIyNDgtNjYyYTZjMGY4YmExIn0.fMTJDorq2-5Aw1QMUFxM2uUqduG-iZNS8HeGCNnM9lAieFPeoEk36W9OA8Z15nGLI-AcNEjjpUFH3evZCJfaMSFOA7P-kcJvVMTcCc57sTNIpKNn8ZzrA8V9XL_n4PDVJFG36BXYmJSUFBEoXwI9deHT4znP5_MLUTOR846ruZLrYneNPAZsM0O4sBfYm6YO4E7qA2krEqTtzjwl9t0YC-7543aSDqk-9xVdSogDc20ljs8SIPPSom01HpRcOpMudUCf7U4-cgvs_pS29f7w4GIHtf59D0T93ZgUkubrvv_Vt63-nhukT_IjzExUjaZEjjfA9pdJ8eH1js-52Suy4A";
		JwtClaims jwtClaims = jwtConsumer.processToClaims(jwt);
		System.out.println(jwtClaims.getSubject());
	}

}
