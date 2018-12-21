package no.nav.sporingslogg.ldap;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LdapGroupService  {
	
    private static final Logger log = LoggerFactory.getLogger(LdapGroupService.class);

    private final String groupToCheckFor;
    private final LdapService ldapService;
    
    public LdapGroupService(String groupToCheckFor) {
    	this.groupToCheckFor = groupToCheckFor;
    	ldapService = new LdapService();
    }
    
    public boolean brukerErIGruppe(String bruker) {
        LdapContext ldapCtx = ldapService.hentLdapContext();
        // Leter i begge trærne uansett om bruker er srv- eller ikke, usikker på om de alltid ligger riktig
        NamingEnumeration<SearchResult> userResults = ldapService.searchUser(ldapCtx, ldapService.getNormalUserSearchBase(), bruker);
        NamingEnumeration<SearchResult> srvUserResults = ldapService.searchUser(ldapCtx, ldapService.getServiceUserSearchBase(), bruker);
        
        try {
            List<String> result = new ArrayList<>();
            result.addAll(hentUtGrupper(userResults));
            result.addAll(hentUtGrupper(srvUserResults));
            return result.contains(groupToCheckFor);
            
        } catch (Exception e) {
            log.error("Error extracting memberOf in ldap", e);
            throw new RuntimeException(e);
            
        } finally {
            ldapService.cleanUpContext(ldapCtx);
        }
    }

    private List<String> hentUtGrupper(NamingEnumeration<SearchResult> searchResults) throws NamingException {
        List<String> resultat = new ArrayList<>();
        if (!searchResults.hasMore()) {
        	return resultat;
        }
        
        Attribute memberOf = searchResults.next().getAttributes().get("memberOf");
        if (memberOf == null) {
        	return resultat;
        }
        
        // Fant noen grupper
        for (int i = 0; i < memberOf.size(); i++) {
            String cn = memberOf.get(i).toString().split(",")[0].trim(); // Hent det som er før komma, dvs gruppenavn må være først i LDAP-strengen
            resultat.add(cn.substring(3)); // Ta bort "CN="
        }
        return resultat;
    }
}
