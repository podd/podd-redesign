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
package com.github.podd.resources;

import java.util.List;

import org.openrdf.model.URI;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddWebServiceApplication;

public abstract class AbstractPoddResourceImpl extends ServerResource
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public AbstractPoddResourceImpl()
    {
        super();
    }
    
    /**
     * Checks the ability of the currently authenticated user to perform the given action and throws
     * an exception if the current user is not authorised for the given action.
     *
     * @param action
     *            The PoddAction that is to be performed.
     * @throws ResourceException
     *             with Status.CLIENT_ERROR_UNAUTHORIZED (HTTP 401) if the user is not authorised to
     *             perform the given action
     */
    protected boolean checkAuthentication(final PoddAction action) throws ResourceException
    {
        // throws an error on failure
        return this.checkAuthentication(action, null, true);
    }
    
    /**
     * Checks the ability of the currently authenticated user to perform the given action and throws
     * an exception if the current user is not authorised for the given action.
     *
     * @param action
     *            The PoddAction that is to be performed.
     * @param optionalObjectUri
     *            A single object URI to be used for authorization, or null if none are needed for
     *            authorization or it could not be determined.
     * @throws ResourceException
     *             with Status.CLIENT_ERROR_UNAUTHORIZED (HTTP 401) if the user is not authorised to
     *             perform the given action
     */
    protected boolean checkAuthentication(final PoddAction action, final URI optionalObjectUri)
        throws ResourceException
    {
        // throws an error on failure
        return this.checkAuthentication(action, optionalObjectUri, true);
    }
    
    /**
     * Checks the ability of the currently authenticated user to perform the given action,
     * optionally throwing an exception instead of returning false in the case that the check fails.
     *
     * @param action
     *            The PoddAction that is to be performed.
     * @param optionalObjectUri
     *            A single object URIs to be used for authorization, or null if none are needed for
     *            authorization or it could not be determined.
     * @param throwExceptionOnFailure
     *            If true, this method throws a ResourceException on failure instead of returning
     *            false
     * @return Returns true if the user is able to perform the given action on the given objects,
     *         and either throws an exception or returns false if they are not able to perform the
     *         given action, depending on the value of the throwExceptionOnFailure parameter.
     * @throws ResourceException
     *             with Status.CLIENT_ERROR_UNAUTHORIZED (HTTP 401) if the user is not authorised to
     *             perform the given action
     */
    protected boolean checkAuthentication(final PoddAction action, final URI optionalObjectUri,
            final boolean throwExceptionOnFailure) throws ResourceException
    {
        if(this.getPoddApplication().authenticate(action, this.getRequest(), this.getResponse(), optionalObjectUri))
        {
            return true;
        }
        else if(throwExceptionOnFailure)
        {
            // Strategies for fixing #81
            // If they have an existing cookie then we tell them to discard it
            // CookieSetting cookie =
            // this.getResponse().getCookieSettings().getFirst(PoddWebConstants.COOKIE_NAME,
            // false);
            // if(cookie != null)
            // {
            // cookie.setMaxAge(0);
            // }
            // TODO: Test the following strategy if the strategy above does not
            // work
            // if(this.getResponse().getCookieSettings().removeAll(PoddWebConstants.COOKIE_NAME,
            // true))
            // {
            // this.getResponse()
            // .getCookieSettings()
            // .add(new CookieSetting(0, PoddWebConstants.COOKIE_NAME, "",
            // this.getRequest().getRootRef()
            // .getPath(), this.getRequest().getResourceRef().getHostDomain(),
            // "Reset cookie", 0,
            // true));
            // }
            this.log.warn("Client unauthorized. Throwing a ResourceException");
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, action.getErrorMessage());
        }
        else
        {
            // do not log this, as it is a normal part of an operation, as
            // evidenced by not wanting
            // to throw an exception
            return false;
        }
    }
    
    /**
     * Sets the data handler for this resource based on the application level data handler.
     *
     * NOTE: This requires the application to be an instance of OasWebServiceApplication for it to
     * function correctly
     */
    @Override
    public synchronized void doInit()
    {
        super.doInit();
    }
    
    /**
     * Determines the action to use based on whether there is a user currently logged in, and
     * whether that user matches the given user identifier parameter.
     *
     * @param requestedUserIdentifier
     *            The user to determine the action for.
     * @param otherUserAction
     *            The action to return if the requested user is not the current user.
     * @param currentUserAction
     *            The action to return if the requested user is the current user.
     * @return The action for the logged in user on the requested user
     */
    protected PoddAction getAction(final String requestedUserIdentifier, final PoddAction otherUserAction,
            final PoddAction currentUserAction)
    {
        PoddAction action = otherUserAction;
        
        if(this.getRequest().getClientInfo().isAuthenticated())
        {
            if(requestedUserIdentifier != null
                    && requestedUserIdentifier.equals(this.getRequest().getClientInfo().getUser().getIdentifier()))
            {
                action = currentUserAction;
            }
        }
        return action;
    }
    
    public PoddWebServiceApplication getPoddApplication()
    {
        final PoddWebServiceApplication application = (PoddWebServiceApplication)super.getApplication();
        
        return application;
    }
    
    public PropertyUtil getPropertyUtil()
    {
        return this.getPoddApplication().getPropertyUtil();
    }
    
    public PoddArtifactManager getPoddArtifactManager()
    {
        return this.getPoddApplication().getPoddArtifactManager();
    }
    
    public PoddRepositoryManager getPoddRepositoryManager()
    {
        return this.getPoddApplication().getPoddRepositoryManager();
    }
    
    public PoddSchemaManager getPoddSchemaManager()
    {
        return this.getPoddApplication().getPoddSchemaManager();
    }
    
    public PoddSesameManager getPoddSesameManager()
    {
        return this.getPoddApplication().getPoddArtifactManager().getSesameManager();
    }
    
    /**
     * Overriding broken ServerResource.getVariants method
     *
     * NOTE: This is not a caching implementation, so the way it is used may cause it to be a
     * performance bottleneck.
     */
    @Override
    protected List<Variant> getVariants(final Method method)
    {
        return super.getVariants(method);
    }
    
}