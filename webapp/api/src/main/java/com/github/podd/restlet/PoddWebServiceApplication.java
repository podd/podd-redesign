package com.github.podd.restlet;

import java.io.InputStream;
import java.util.Collection;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
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
     * Checking client authorization for the given action and optional set of Object URIs is also
     * performed by this method.
     * 
     * Only call this method if authentication is needed for the given request.
     * 
     * @param action
     *            The action to perform
     * @param request
     * @param response
     * @param optionalObjectUris
     *            Optional set of Object URIs on which the action is to be performed. If present,
     *            these should be used to check client authority.
     * @return True if the request is authenticated, and false otherwise.
     */
    public abstract boolean authenticate(PoddAction action, Request request, Response response,
            Collection<URI> optionalObjectUris);
    
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
    public abstract PoddSesameRealm getRealm();
    
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
    
    public abstract void setPoddArtifactManager(PoddArtifactManager poddArtifactManager);
    
    public abstract void setPoddRepositoryManager(PoddRepositoryManager poddRepositoryManager);
    
    public abstract void setPoddSchemaManager(PoddSchemaManager poddSchemaManager);
    
    public abstract void setRealm(PoddSesameRealm realm);
    
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
    
    public abstract Model getAliasesConfiguration();
    
    public abstract void setAliasesConfiguration(Model aliasesConfiguration);
    
}