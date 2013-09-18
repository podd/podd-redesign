package com.github.podd.resources;

import org.restlet.resource.ResourceException;

import com.github.podd.utils.PoddWebConstants;

public abstract class AbstractUserResourceImpl extends AbstractPoddResourceImpl
{
    
    public AbstractUserResourceImpl()
    {
        super();
    }
    
    /**
     * 
     * @return The user parameter based primarily on the query parameters or as a backup based on
     *         the logged in user. Returns null if it does not exist as a query parameter and the
     *         user is not logged in.
     * @throws ResourceException
     */
    protected String getUserParameter() throws ResourceException
    {
        String requestedUserIdentifier = this.getQuery().getFirstValue(PoddWebConstants.KEY_USER_IDENTIFIER, true);
        
        if(requestedUserIdentifier == null)
        {
            if(this.getRequest().getClientInfo().isAuthenticated())
            {
                // Default to requesting information about the logged in user
                requestedUserIdentifier = this.getRequest().getClientInfo().getUser().getIdentifier();
            }
            else
            {
                this.log.error("Did not specify user and not logged in, returning null");
                // no identifier specified.
                // throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                // "Did not specify user");
            }
        }
        return requestedUserIdentifier;
    }
    
}