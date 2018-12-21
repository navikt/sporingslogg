package no.nav.sporingslogg.ldap;

import java.util.Arrays;

public class CredentialExtractor {

	// Ikke sikker på hva denne koden egeentlig gjør, ser ut som den håndterer cred i form av char[] ved å fjerne komma og lese annethvert tegn.....
	// Kopiert fra andre...
    public static String extractPassword(Object webCredential) {
        return (webCredential instanceof String)
                ? (String) webCredential
                : decryptUserPass(webCredential);
    }

    private static String decryptUserPass(Object webCredential) {
        String s = Arrays.toString(((char[]) webCredential));
        s = s.substring(1, s.length() - 1);
        s = s.replaceAll(",", "").trim();
        String result = "";
        for (int i = 0; i < s.length(); i += 2) {
            char rep = s.charAt(i);
            result += rep;
        }
        return result;
    }

}
