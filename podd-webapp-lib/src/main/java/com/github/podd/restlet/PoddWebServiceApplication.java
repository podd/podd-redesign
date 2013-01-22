package com.github.podd.restlet;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.ChallengeAuthenticator;

import com.github.ansell.restletutils.RestletUtilSesameRealm;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;

import freemarker.template.Configuration;

/**
 * The application which is the parent of all Podd Restlet resources. It provides the application
 * level functions for restlets including the DataHandler and the authentication implementation.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 *         Copied from OAS project (https://github.com/ansell/oas)
 */
public abstract class PoddWebServiceApplication extends Application
{
    
    /**
     * Checks whether the client is authenticated for the given action, and if they are not, the
     * response will have challenges inserted and the status will be set to HTTP 401.
     * 
     * Only call this method if authentication is needed for the given request.
     * 
     * @param authentication
     * @param request
     * @param response
     * @return True if the request is authenticated, and false otherwise.
     */
    public abstract boolean authenticate(PoddAction authentication, Request request, Response response);
    
    /**
     * 
     * @return The ChallengeAuthenticator which is currently being used to respond to queries that
     *         require authentication.
     */
    public abstract ChallengeAuthenticator getAuthenticator();
    
    public abstract PoddArtifactManager getPoddArtifactManager();
    
    public abstract PoddRepositoryManager getPoddRepositoryManager();
    
    public abstract PoddSchemaManager getPoddSchemaManager();
    
    /**
     * Gets the realm which is used to manage users and roles.
     * 
     * @return
     */
    public abstract RestletUtilSesameRealm getRealm();
    
    /**
     * Returns the FreeMarker template configuration object for this application.
     * 
     * @return
     */
    public abstract Configuration getTemplateConfiguration();
    
    /**
     * 
     * @param auth
     *            The new ChallengeAuthenticator to be used to respond to queries which require
     *            authentication.
     */
    public abstract void setAuthenticator(ChallengeAuthenticator auth);
    
    /**
     * Sets the realm which is used to manage users and roles.
     * 
     * @param nextRealm
     */
    public abstract void setRealm(RestletUtilSesameRealm nextRealm);
    
    /**
     * Set a new Freemarker Template Configuration for this application.
     * 
     * @param nextFreemarkerConfiguration
     */
    public abstract void setTemplateConfiguration(Configuration nextFreemarkerConfiguration);
}