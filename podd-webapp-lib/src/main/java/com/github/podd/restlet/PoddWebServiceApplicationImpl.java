package com.github.podd.restlet;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.RestletUtilSesameRealm;
import com.github.podd.resources.AboutResourceImpl;
import com.github.podd.resources.CookieLoginResourceImpl;
import com.github.podd.resources.IndexResourceImpl;
import com.github.podd.resources.UserDetailsResourceImpl;

import freemarker.template.Configuration;

/**
 * This class handles all requests from clients to the OAS Web Service.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * Copied from OAS project (https://github.com/ansell/oas)
 * 
 */
public class PoddWebServiceApplicationImpl extends PoddWebServiceApplication
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * The Freemarker template configuration.
     */
    private volatile Configuration freemarkerConfiguration;
    private volatile ChallengeAuthenticator auth;
    private volatile RestletUtilSesameRealm realm;
    
    /**
     * Default Constructor.
     * 
     * Adds the necessary file protocols and sets up the template location.
     */
    public PoddWebServiceApplicationImpl()
    {
        super();
        
        // List of protocols required by the application
        this.getConnectorService().getClientProtocols().add(Protocol.HTTP);
        this.getConnectorService().getClientProtocols().add(Protocol.CLAP);
        
        // Define extensions for RDF and Javascript
        // These extensions are also used to identify mediatypes in services
        // For example: @Get("owl") will not be processed without the declaration below
        this.getMetadataService().addExtension("rdf", MediaType.APPLICATION_RDF_XML, true);
        this.getMetadataService().addExtension("rj", RestletUtilMediaType.APPLICATION_RDF_JSON, true);
        this.getMetadataService().addExtension("owl", MediaType.APPLICATION_RDF_XML, false);
        this.getMetadataService().addExtension("json", MediaType.APPLICATION_JSON, true);
        this.getMetadataService().addExtension("ttl", MediaType.APPLICATION_RDF_TURTLE, true);
        this.getMetadataService().addExtension("n3", MediaType.TEXT_RDF_N3, true);
        this.getMetadataService().addExtension("nt", MediaType.TEXT_RDF_NTRIPLES, true);
        this.getMetadataService().addExtension("nq",
                MediaType.register("text/nquads", "The NQuads extension to the NTriples RDF serialisation"), true);
        
        this.getMetadataService().addExtension("js", MediaType.TEXT_JAVASCRIPT, true);
        this.getMetadataService().addExtension("css", MediaType.TEXT_CSS, true);
        
        this.getMetadataService().addExtension("multipart", MediaType.MULTIPART_FORM_DATA, true);
        this.getMetadataService().addExtension("form", MediaType.APPLICATION_WWW_FORM, false);
        
        // Automagically tunnel client preferences for extensions through the
        // tunnel
        this.getTunnelService().setExtensionsTunnel(true);
    }
    
    @Override
    public boolean authenticate(final PoddAction authenticationScope, final Request request, final Response response)
    {
        if(authenticationScope.isAuthRequired() && !request.getClientInfo().isAuthenticated())
        {
            if(this.getAuthenticator() == null)
            {
                throw new RuntimeException("Could not find authentication method");
            }
            
            // add challenges to the response and set the status to HTTP 401 Unauthorized
            this.getAuthenticator().challenge(response, false);
            
            // Return false after the challenge and HTTP 401 response have been added to the
            // response
            return false;
        }
        else if(authenticationScope.isAuthRequired() && request.getClientInfo().isAuthenticated()
                && authenticationScope.isRoleRequired()
                && !authenticationScope.matchesForRoles(request.getClientInfo().getRoles()))
        {
            this.log.error("Authenticated user does not have enough privileges to execute the given action");
            
            // FIXME: Implement auditing here
            // this.getDataHandler().addLogDetailsForRequest(message, referenceUri,
            // authenticationScope, get, currentUser, currentRole);
            
            return false;
        }
        
        if(request.getClientInfo().isAuthenticated() && request.getClientInfo().getRoles().isEmpty())
        {
            this.log.warn("Authenticated user did not have any roles: user={}", request.getClientInfo().getUser());
        }
        

        return true;
    }
    
    /**
     * Create the necessary connections between the application and its handlers.
     */
    @Override
    public Restlet createInboundRoot()
    {
        final ChallengeAuthenticator authenticator = this.getAuthenticator();
        
        if(authenticator == null)
        {
            throw new RuntimeException("Could not find authentication method");
        }
        
        final Router router = new Router(this.getContext());
        
        // Add a route for Login form. Login service is handled by the authenticator
        final String login = "login";
        // PropertyUtil.getProperty(PropertyUtils.PROPERTY_LOGIN_FORM_PATH, PoddPropertyUtils.DEFAULT_LOGIN_FORM_PATH);
        this.log.info("attaching login service to path={}", login);
        
        // NOTE: This only displays the login form. All HTTP POST requests to the login path should
        // be handled by the Authenticator
        router.attach(login, CookieLoginResourceImpl.class);
        //router.attach("submitlogininfo", CookieLoginResourceImpl.class);

        // Add a route for the About page.
        final String about = "about";
        this.log.info("attaching about service to path={}", about);
        router.attach(about, AboutResourceImpl.class);

        // Add a route for the Index page.
        final String index = "index";
        this.log.info("attaching index service to path={}", index);
        router.attach(index, IndexResourceImpl.class);
        
        // Add a route for the Index page.
        final String userDetails = "userdetails";
        this.log.info("attaching user details service to path={}", userDetails);
        router.attach(userDetails, UserDetailsResourceImpl.class);
        
        // Add a route for Logout service
        // final String logout = "logout";
        // PropertyUtils.getProperty(PropertyUtils.PROPERTY_LOGOUT_FORM_PATH, PropertyUtils.DEFAULT_LOGOUT_FORM_PATH);
        // this.log.info("attaching logout service to path={}", logout);
        // FIXME: Switch between the logout resource implementations here based on the authenticator
        // router.attach(logout, CookieLogoutResourceImpl.class);
        
        this.log.info("routes={}", router.getRoutes().toString());
        
        // put the authenticator in front of the resource router so it can handle challenge
        // responses and forward them on to the right location after locking in the authentication
        // data Authentication of individual methods on individual resources is handled using calls
        // to OasWebServiceApplication.authenticate
        authenticator.setNext(router);
        
        return authenticator;
    }
    
    /**
     * Fetches a ChallengeAuthenticator based on the key defined in
     * PropertyUtils.PROPERTY_CHALLENGE_AUTH_METHOD.
     * 
     * Currently defaults to a DigestAuthenticator.
     * 
     * @return A ChallengeAuthenticator that can be used to challenge unauthenticated requests to
     *         resources that need authenticated access.
     */
    @Override
    public ChallengeAuthenticator getAuthenticator()
    {
        return this.auth;
    }
    
    
    @Override
    public RestletUtilSesameRealm getRealm()
    {
        return this.realm;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * net.maenad.oas.webservice.impl.OasWebServiceApplicationInterface#getTemplateConfiguration()
     */
    @Override
    public Configuration getTemplateConfiguration()
    {
        return this.freemarkerConfiguration;
    }
    
    /**
     * @param auth
     *            the auth to set
     */
    @Override
    public void setAuthenticator(final ChallengeAuthenticator auth)
    {
        this.auth = auth;
    }
    
    @Override
    public void setRealm(final RestletUtilSesameRealm nextRealm)
    {
        this.realm = nextRealm;
    }
    
    @Override
    public void setTemplateConfiguration(final Configuration nextFreemarkerConfiguration)
    {
        this.freemarkerConfiguration = nextFreemarkerConfiguration;
    }
    
}
