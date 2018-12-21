package no.nav.sporingslogg.ldap;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.sporingslogg.standalone.PropertyNames;
import no.nav.sporingslogg.standalone.PropertyUtil;

public class LdapService {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final Properties props;
	
    public LdapService() {
		this.props = getLdapProps(PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_USERNAME), PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_PASSWORD));
	}

	private Properties getLdapProps(String user, String pw) {
		Properties props = new Properties();
		props.setProperty(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        props.setProperty(Context.PROVIDER_URL, PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_URL));
        props.setProperty(Context.SECURITY_PRINCIPAL, user);
        props.setProperty(Context.SECURITY_CREDENTIALS, pw);
        props.setProperty(Context.SECURITY_AUTHENTICATION, "simple");
//      props.setProperty("com.sun.jndi.ldap.connect.pool", "true");
        return props;
	}

    public String getNormalUserSearchBase() {
    	return PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_USER_BASEDN);
    }
    public String getServiceUserSearchBase() {
    	return PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_SERVICEUSER_BASEDN);
    }
    public String getSearchBase(String username) {
    	if (username.startsWith("srv")) {
    		return getServiceUserSearchBase();
    	}
    	return getNormalUserSearchBase();
    }

    
	public LdapContext hentLdapContext() {  // TODO nok å gjøre 1 gang ?
        try {
            return new InitialLdapContext(props, new Control[0]);
        } catch (Exception e) {
            throw new RuntimeException("Kan ikke koble til LDAP "+PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_URL)+" med user: " + PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_USER_BASEDN), e);
        }
    }

	public LdapContext hentLdapContext(String user, String pw) {  
		Properties userSpecificProps = getLdapProps(user, pw);
        try {
            return new InitialLdapContext(userSpecificProps, new Control[0]);
        } catch (Exception e) {
            throw new RuntimeException("Kan ikke koble til LDAP "+PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_URL)+" med user: " + user, e);
        }
    }
	
    public NamingEnumeration<SearchResult> searchUser(LdapContext ctx, String ldapSearchBase, String user) {
        String searchFilter = "(cn=" + user + ")";
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        try {
			return ctx.search(ldapSearchBase, searchFilter, searchControls);
		} catch (NamingException e) {
			throw new RuntimeException("LDAP search for user " + user + "fails", e);
		}
    }

    public void cleanUpContext(LdapContext ctx) {
        try {
            if (ctx != null) {
                ctx.close();
            }
        } catch (Exception e) {
            log.warn("Error when trying to close ldap context", e);
        }
    }
}
