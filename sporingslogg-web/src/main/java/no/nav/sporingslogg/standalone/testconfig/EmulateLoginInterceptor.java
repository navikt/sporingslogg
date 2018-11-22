package no.nav.sporingslogg.standalone.testconfig;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.apache.cxf.common.security.SimplePrincipal;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.security.SecurityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * Brukes på f.eks. Jetty hvor det ikke gjøres noen login mot LDAP. 
 * Tar brukernavn fra Basic-auth HTTP-header, legger som Principal i SecurityContext.
 */
public class EmulateLoginInterceptor extends AbstractPhaseInterceptor<Message> {

    private Logger log = LoggerFactory.getLogger(getClass());

    public static String USER_TO_REFUSE = null;
    
    public EmulateLoginInterceptor() {
        this(true);
    }
    
    public EmulateLoginInterceptor(boolean uniqueId) {
        super(null, Phase.PRE_INVOKE, uniqueId);
    }
    
    public void handleMessage(Message message) throws Fault {
        log.info("Legger inn bruker fra Basic-auth header i SecurityContext.Principal");

        SecurityContext sc = message.get(SecurityContext.class);        
        if (sc != null && sc.getUserPrincipal() != null) {
            log.info("Det finnes en SecurityContext.Principal allerede, dropper å legge inn bruker fra Basic-auth header");
            return;
        }
        
        @SuppressWarnings("unchecked")
		Map<String, List<String>> allHeaders = (Map<String, List<String>>) message.get(Message.PROTOCOL_HEADERS);
        List<String> authHeaders = allHeaders.get(HttpHeaders.AUTHORIZATION);
        String userName = BasicAuthHeaderUtil.getUserName(authHeaders);
        if (userName == null) {
            userName = "NO_LOGGED_IN_USER";
        }
        
        if (USER_TO_REFUSE == null || !USER_TO_REFUSE.equalsIgnoreCase(userName)) {
	        sc = createSecurityContextMedUser(userName);
	        message.put(SecurityContext.class, sc);
	        log.info("La inn bruker fra Basic-auth header: " + userName);
        } else {
	        log.info("La IKKE inn UNAUTH-bruker fra Basic-auth header: " + userName);
	        throw new Fault(new SecurityException("Authentication failed"));
        }
    }

    private SecurityContext createSecurityContextMedUser(String userName) {
        return new SecurityContext() {            
            @Override
            public boolean isUserInRole(String role) {
                return true;
            }            
            @Override
            public Principal getUserPrincipal() {
                return new SimplePrincipal(userName);
            }
        };
    }  
}
