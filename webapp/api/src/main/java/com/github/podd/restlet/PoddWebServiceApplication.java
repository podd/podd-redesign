/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.restlet;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.security.ChallengeAuthenticator;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.file.PoddDataRepository;
import com.github.podd.api.file.PoddDataRepositoryManager;

import freemarker.template.Configuration;

/**
 * The application which is the parent of all Podd Restlet resources. It provides the application
 * level functions for restlets including the {@link PoddArtifactManager} and the authentication
 * implementation.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
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
     *            The Restlet {@link Request} matching the given action.
     * @param response
     *            The Restlet {@link Response} matching the given action.
     * @param optionalObjectUris
     *            Optional set of Object URIs on which the action is to be performed. If present,
     *            these should be used to check client authority.
     * @return True if the request is authenticated, and false otherwise.
     */
    public abstract boolean authenticate(PoddAction action, Request request, Response response, URI optionalObjectUri);
    
    /**
     * Retrieves the data repository configuration used by this server.
     * 
     * @return A {@link Model} containing the RDF statements for the data repository configuration
     *         for this server.
     */
    public abstract Model getDataRepositoryConfig();
    
    /**
     * 
     * @return The ChallengeAuthenticator which is currently being used to respond to queries that
     *         require authentication.
     */
    public abstract ChallengeAuthenticator getAuthenticator();
    
    /**
     * Get the {@link PoddArtifactManager} used by this application to manage artifacts.
     * 
     * @return The {@link PoddArtifactManager} used by this application.
     */
    public abstract PoddArtifactManager getPoddArtifactManager();
    
    /**
     * Get the {@link PoddDataRepositoryManager} used by this application to manage data repository
     * access and verification.
     * 
     * @return The {@link PoddDataRepositoryManager} used by this application.
     */
    public abstract PoddDataRepositoryManager getPoddDataRepositoryManager();
    
    /**
     * Get the {@link PoddRepositoryManager} used by this application to manage access to OpenRDF
     * Sesame {@link Repository} objects.
     * 
     * @return The {@link PoddRepositoryManager} used by this application.
     */
    public abstract PoddRepositoryManager getPoddRepositoryManager();
    
    /**
     * Get the {@link PoddSchemaManager} used by this application to manage schema ontologies.
     * 
     * @return The {@link PoddSchemaManager} used by this application.
     */
    public abstract PoddSchemaManager getPoddSchemaManager();
    
    /**
     * Get the {@link PropertyUtil} used by this application for contextual settings.
     * 
     * @return The {@link PropertyUtil} instance for this {@link PoddWebServiceApplication}.
     */
    public abstract PropertyUtil getPropertyUtil();
    
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
     * Sets the {@link PoddDataRepository} mapping information for this application.
     * 
     * @param aliasesConfiguration
     *            The mappings for data repositories that are relevant to this application.
     */
    public abstract void setDataRepositoryConfig(Model aliasesConfiguration);
    
    /**
     * 
     * @param auth
     *            The new ChallengeAuthenticator to be used to respond to queries which require
     *            authentication.
     */
    public abstract void setAuthenticator(ChallengeAuthenticator auth);
    
    /**
     * Set a new {@link PoddArtifactManager} to use for managing PODD artifacts for this
     * application.
     * 
     * @param poddArtifactManager
     *            The artifact manager
     */
    public abstract void setPoddArtifactManager(PoddArtifactManager poddArtifactManager);
    
    /**
     * 
     * @param nextDataRepositoryManager
     *            A manager for {@link PoddDataRepository} objects used to host external data
     *            references for PODD Artifacts.
     */
    public abstract void setPoddDataRepositoryManager(PoddDataRepositoryManager nextDataRepositoryManager);
    
    /**
     * 
     * @param poddRepositoryManager
     *            A manager for OpenRDF {@link Repository} objects used by the application to store
     *            and retrieve data.
     */
    public abstract void setPoddRepositoryManager(PoddRepositoryManager poddRepositoryManager);
    
    /**
     * Set a new {@link PoddSchemaManager} to use for managing schema ontologies for this
     * application.
     * 
     * @param poddSchemaManager
     *            The schema ontology manager
     */
    public abstract void setPoddSchemaManager(PoddSchemaManager poddSchemaManager);
    
    /**
     * Set a new {@link PoddSesameRealm} to use for authentication for this application.
     * 
     * @param realm
     *            The authentication realm.
     */
    public abstract void setRealm(PoddSesameRealm realm);
    
    /**
     * Set a new Freemarker Template Configuration for this application.
     * 
     * @param nextFreemarkerConfiguration
     */
    public abstract void setTemplateConfiguration(Configuration nextFreemarkerConfiguration);
}