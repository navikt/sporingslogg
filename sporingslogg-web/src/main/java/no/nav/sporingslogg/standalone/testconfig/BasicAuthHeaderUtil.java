package no.nav.sporingslogg.standalone.testconfig;

import java.util.Base64;
import java.util.List;

class BasicAuthHeaderUtil {

    static String getUserName(List<String> basicAuthHeaders) {
        try {
            String authHeader = basicAuthHeaders.get(0);
            String encodedString = authHeader.substring("Basic".length()).trim();
            String decodedString = decode(encodedString);
            String[] credentials = decodedString.split(":");
            return credentials[0];
        } catch (Exception e) {
            return null; 
        }
    }

    private static String decode(String s) {
        return new String(Base64.getDecoder().decode(s));  
    }
}
