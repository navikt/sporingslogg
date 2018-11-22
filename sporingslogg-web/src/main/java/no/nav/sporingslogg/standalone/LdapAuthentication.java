package no.nav.sporingslogg.standalone;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LdapAuthentication {

    private static final Logger log = LoggerFactory.getLogger(LdapAuthentication.class);
    private static final String contextFactory = "com.sun.jndi.ldap.LdapCtxFactory";

    public String authenticateUser(String username, String password, String userBaseDn) {
//    	log.debug("Autentiserer " + username + " " + password + " " + userBaseDn);
    	String ldapUser = PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_USERNAME);
    	String pw = PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_PASSWORD);
    	InitialDirContext serviceContext;
        try {
//        	log.debug("Trying LDAP at " +PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_URL)+ " with " + ldapUser + " " + pw);
            serviceContext = new InitialDirContext(getLdapProperties(ldapUser, pw));
        } catch (Exception ex) {
            log.error("Cannot connect to LDAP with service user ", ldapUser);
            return null;
        }
        return findUserByName(serviceContext, userBaseDn, username, password);
    }

    private Properties getLdapProperties(String username, String password) {
    	Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, contextFactory);
        props.put(Context.PROVIDER_URL, PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_URL));
        props.put(Context.SECURITY_AUTHENTICATION, "simple");
        props.put(Context.SECURITY_PRINCIPAL, username);
        props.put(Context.SECURITY_CREDENTIALS, password);
        return props;
    }

    private String findUserByName(DirContext ctx, String ldapSearchBase, String user, String password) {
        String searchFilter = "(cn=" + user + ")";
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        String distinguishedName = "NONE";

        try {
            NamingEnumeration<SearchResult> results = ctx.search(ldapSearchBase, searchFilter, searchControls);
            if (results.hasMore()) {
                SearchResult result = results.next();
                distinguishedName = result.getNameInNamespace();
                new InitialDirContext(getLdapProperties(distinguishedName, password));
                return user;
            }
            return null;

        } catch (Exception ex) {
            log.error("User with DN: " + distinguishedName + ", could not be Authenticated", ex);
            return null;
        } finally {
            cleanUp(ctx);
        }
    }

    private void cleanUp(DirContext ldapCtx) {
        try {
            if (ldapCtx != null) {
                ldapCtx.close();
            }
        } catch (Exception e) {
            log.error("Error when trying to close ldap context", e);
        }
    }
}