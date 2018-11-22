package no.nav.sporingslogg.standalone;

import java.util.Arrays;
import java.util.Map;

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
    private static final Logger log = LoggerFactory.getLogger(LdapLoginModule.class);
    private String stringPassword;

    public LdapLoginModule() {
    }

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

            String webUserName = ((NameCallback) callbacks[0]).getName();
            Object webCredential = ((ObjectCallback) callbacks[1]).getObject();

            if (webUserName == null || webCredential == null) {
                return nonGivenCredentials();
            }

            stringPassword = extractPassword(webCredential);
            UserInfo userInfo = getUserInfo(webUserName);
            if (userInfo == null) {
                return notAnAuthenticatedUser(webUserName);
            }

            // Setting up for the loginmodule to find user in further operations
            setCurrentUser(new JAASUserInfo(userInfo));
            return grantUserIfAuthenticated();

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
        String rightUserBaseDn = getBasednForUsertype(username);
        String authenticatedUser = new LdapAuthentication().authenticateUser(username, stringPassword, rightUserBaseDn);
        if (authenticatedUser == null) {
            return null;
        }
        return new UserInfo(authenticatedUser, Credential.getCredential(stringPassword));
    }

    private boolean nonGivenCredentials(){
        setAuthenticated(false);
        log.error("You need to provide both password and username to login, check your credentials");
        return isAuthenticated();
    }

    private boolean notAnAuthenticatedUser(String webUserName){
        setAuthenticated(false);
        log.error(webUserName + ": is not Authenticated - tried binding to LDAP: " + PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_URL));
        return false;
    }

    private String extractPassword(Object webCredential) {
        return (webCredential instanceof String)
                ? (String) webCredential
                : decryptUserPass(webCredential);
    }

    private String decryptUserPass(Object webCredential) {
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

    private String getBasednForUsertype(String username) {
    	if (username.startsWith("srv")) {
    		return PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_SERVICEUSER_BASEDN);
    	}
    	return PropertyUtil.getProperty(PropertyNames.PROPERTY_LDAP_USER_BASEDN);
    }

    private boolean grantUserIfAuthenticated() throws Exception {
        setAuthenticated(getCurrentUser().checkCredential(stringPassword));
        Boolean authenticated = isAuthenticated();
        if (authenticated) {
            getCurrentUser().fetchRoles();
        }
        return authenticated;
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