package no.nav.sporingslogg.oidc;

import java.security.Key;

import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

public class OidcTokenGeneratorUtil {

	public static void main(String[] args) {
		String user = "12345678901";
		System.out.println("Token for bruker '"+user+"': " + generateDummyToken(user));
	}
	
	public static String generateDummyToken(String user) {
		try {
			JwtClaims generateOkClaims = generateClaims(user, "dummyIssuer");
			return generateToken(generateOkClaims, "dummyKey_som_er_lenger_enn_64_bytes");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static JwtClaims generateClaims(String subject, String issuer) throws JoseException {
	    final JwtClaims claims = new JwtClaims();
	    claims.setSubject(subject);
	    claims.setExpirationTimeMinutesInTheFuture(5);
	    claims.setIssuer(issuer);
	    claims.setStringClaim("acr", "Level4");
	    return claims;
	}
	
	public static String generateToken(JwtClaims claims, String key) throws JoseException {
	    return generateToken(claims, getKey(key));
	}
	
	public static String generateToken(JwtClaims claims, Key key) throws JoseException {
	    final JsonWebSignature jws = new JsonWebSignature();
	    jws.setPayload(claims.toJson());
	    jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA256);
	    jws.setKey(key);
	    return jws.getCompactSerialization();
	}
	
	public static Key getKey(String key) {
		return new HmacKey(key.getBytes());
	}
}
