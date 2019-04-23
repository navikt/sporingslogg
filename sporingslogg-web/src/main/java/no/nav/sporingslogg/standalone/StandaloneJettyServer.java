package no.nav.sporingslogg.standalone;

import java.io.IOException;

import org.eclipse.jetty.jaas.JAASLoginService;
import org.eclipse.jetty.plus.jndi.Resource;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.FragmentConfiguration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.MetaInfConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.webapp.WebInfConfiguration;
import org.eclipse.jetty.webapp.WebXmlConfiguration;
import org.springframework.web.context.ContextLoaderListener;

import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.DefaultExports;

//import io.prometheus.client.hotspot.DefaultExports;


public class StandaloneJettyServer { 
// Standalone Jetty, både for ekte kjøring på Nais og lokalt for test

	private static final String JETTY_HOST = "0.0.0.0";
	private static final int JETTY_DEFAULT_PORT = 8088; // brukes i Dockerfile og nais.yaml, kan overstyres for test
	private static final int JETTY_IDLE_TIMEOUT = 30000;
	
	private static final String CONTEXT_ROOT = "/sporingslogg";
	private static final String WEBAPP_ROOT = "/webapp/";

    private static final String CREDENTIAL_PROPS = "/webapp/WEB-INF/login.conf"; 
    private static final String LOGIN_MODULE = "ldaploginmodule";
    
    private static final String DB_RESOURCE_JNDI_NAME = "jdbc/sporingsloggDS";
    //private static final String JETTY_EMBEDDED_ATTRIBUTES_NAME = "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern";
    private static final String JETTY_EMBEDDED_ATTRIBUTES_VALUE = "^.*cxf-.*.jar$|^.*webapp-.*.jar$|.*/classes/.*";

    private static final Configuration[] CONFIGURATIONS = new Configuration[]{
            new WebInfConfiguration(),
            new WebXmlConfiguration(),
            new MetaInfConfiguration(),
            new FragmentConfiguration(),
            new EnvConfiguration(),
            new PlusConfiguration(),
//            new AnnotationConfiguration(),
            new JettyWebXmlConfiguration()
    };

    private final Server server;
    private final String webXml;
    
    public StandaloneJettyServer(String webXml) {
        this(webXml, JETTY_DEFAULT_PORT);        
    }

    public StandaloneJettyServer(String webXml, int port) {
        server = new Server();
        this.webXml = webXml;
        ServerConnector serverConnector = new ServerConnector(server);
        serverConnector.setHost(JETTY_HOST);
        serverConnector.setPort(port);
        serverConnector.setIdleTimeout(JETTY_IDLE_TIMEOUT);
        server.addConnector(serverConnector);
        
        System.setProperty("java.security.auth.login.config", getClass().getResource(CREDENTIAL_PROPS).toString());
        
    }

    public void createContextHandler(Object datasourceResource) throws Exception{
        WebAppContext webapp = initContext();
        setSecurityHandler(webapp, LOGIN_MODULE);
        webapp.setParentLoaderPriority(true);
    	new Resource(webapp, DB_RESOURCE_JNDI_NAME, datasourceResource);
        server.setHandler(webapp);
     }

    private WebAppContext initContext() throws Exception{
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath(CONTEXT_ROOT);
        webAppContext.setResourceBase(StandaloneJettyServer.class.getResource(WEBAPP_ROOT).toExternalForm());
        webAppContext.addEventListener(new ContextLoaderListener());
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.useFileMappedBuffer", "false");
        webAppContext.setDescriptor(getClass().getResource(webXml).toString());
        webAppContext.setConfigurations(CONFIGURATIONS);
        //webAppContext.setAttribute(WebInfConfiguration.CONTAINER_JAR_PATTERN, JETTY_EMBEDDED_ATTRIBUTES_VALUE);
        
        webAppContext.addServlet(MetricsServlet.class, "/prometheus");
        DefaultExports.initialize();
        
        return webAppContext;
    }

    public void setSecurityHandler(WebAppContext webAppContext, String loginModuleName) throws IOException {
        JAASLoginService loginService = new JAASLoginService("JAAS Login");
        loginService.setLoginModuleName(loginModuleName);
        //loginService.setIdentityService(new DefaultIdentityService());
        ConstraintSecurityHandler sh = new ConstraintSecurityHandler();
        sh.setLoginService(loginService);
        sh.setDenyUncoveredHttpMethods(true);
        webAppContext.setSecurityHandler(sh);
    }
    
    public void startJetty(boolean rejoin) throws Exception {
        server.start();
        if (rejoin) {
        	server.join();
        }
    }
}