package no.nav.sporingslogg.ldap;

import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;

import org.eclipse.jetty.jaas.callback.ObjectCallback;
import org.eclipse.jetty.jaas.spi.AbstractLoginModule;
import org.eclipse.jetty.jaas.spi.UserInfo;
import org.eclipse.jetty.util.security.Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LdapLoginModule extends AbstractLoginModule {
	
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private String stringPassword; // Brukes implisitt av overridden-metoder som ikke kan ta den som input
    private final LdapService ldapService;
    
    public LdapLoginModule() {
    	ldapService = new LdapService();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map sharedState, Map options) {
        super.initialize(subject, callbackHandler, sharedState, options);
    }

    @Override
    public boolean login() throws LoginException {
        try {
            if (getCallbackHandler() == null) {
                log.error("Interceptor Error: Check your login details. If you use basic Auth, check that your credentials are given in header");
                throw new LoginException("Callback handler not created, no credentials where given");
            }
            Callback[] callbacks = configureCallbacks();
            getCallbackHandler().handle(callbacks);

            String username = ((NameCallback) callbacks[0]).getName();
            Object credential = ((ObjectCallback) callbacks[1]).getObject();
            if (username == null || credential == null) {
                setAuthenticated(false);
                return false;
            }

            stringPassword = CredentialExtractor.extractPassword(credential);
            UserInfo userInfo = getUserInfo(username);
            if (userInfo == null) { // Kan ikke hente bruker i LDAP med angitt cred
                setAuthenticated(false);
                return false;
            }

            // Oppsett så loginmodule finner brukeren i resten av dialogen, vet ikke helt hvorfor dette er akkurat sånn
            setCurrentUser(new JAASUserInfo(userInfo)); 
            setAuthenticated(getCurrentUser().checkCredential(stringPassword));
            Boolean authenticated = isAuthenticated();
            if (authenticated) {
                getCurrentUser().fetchRoles();
            }
            return authenticated;

        } catch (UnsupportedCallbackException e) {
            log.error("Error obtaining callback information - Not valid details. Check your details");
            throw new LoginException("Error obtaining callback information");
            
        } catch (Exception e) {
            log.error("Error obtaining user info - User is not found in ldap - could not Authenticate");
            throw new LoginException("Error obtaining user info -  User is not found in ldap.");
        }
    }

    @Override
    public UserInfo getUserInfo(String username) {
        String searchBase = ldapService.getSearchBase(username);
        String authenticatedUser = findInLdap(username, stringPassword, searchBase);
        if (authenticatedUser == null) {
            return null;
        }
        return new UserInfo(authenticatedUser, Credential.getCredential(stringPassword));
    }

    private String findInLdap(String username, String password, String userBaseDn) {
    	LdapContext serviceContext = null;
    	try {
	    	serviceContext = ldapService.hentLdapContext();
	        NamingEnumeration<SearchResult> results = ldapService.searchUser(serviceContext, userBaseDn, username);
	        if (results.hasMore()) {
	            SearchResult result = results.next();
	            ldapService.hentLdapContext(result.getNameInNamespace(), password);
	            return username;
	        }
	        return null;
        } catch (NamingException e) {
			// Bare returner null, betyr at brukeren ikke kan finnes/autentiseres
        	return null;
		} finally {
			ldapService.cleanUpContext(serviceContext);
        }
    }

    @Override
    public boolean commit() throws LoginException {
        return super.commit();
    }

    @Override
    public boolean abort() throws LoginException {
        return super.abort();
    }
}