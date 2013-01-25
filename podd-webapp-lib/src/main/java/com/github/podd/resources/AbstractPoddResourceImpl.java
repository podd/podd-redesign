package com.github.podd.resources;

import java.util.Collection;
import java.util.List;

import org.openrdf.model.URI;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddWebServiceApplication;

public abstract class AbstractPoddResourceImpl extends ServerResource
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    public AbstractPoddResourceImpl()
    {
        super();
    }
    
    /**
     * Checks the ability of the currently authenticated user to perform the given action.
     * 
     * @param action
     *            The PoddAction that is to be performed.
     * @param optionalObjectUris 
     *            Optional collection of object URIs to be used for authorization.
     * @throws ResourceException
     *             with Status.CLIENT_ERROR_UNAUTHORIZED (HTTP 401) if the user is not authorised to
     *             perform the given action
     */
    protected void checkAuthentication(final PoddAction action, Collection<URI> optionalObjectUris) throws ResourceException
    {
        
        if(!this.getPoddApplication().authenticate(action, this.getRequest(), this.getResponse(), optionalObjectUris))
        {
            this.log.warn("Client unauthorized. Throwing a ResourceException");
            throw new ResourceException(Status.CLIENT_ERROR_UNAUTHORIZED, action.getErrorMessage());
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
    
    
    public PoddWebServiceApplication getPoddApplication()
    {
        final PoddWebServiceApplication application = (PoddWebServiceApplication) super.getApplication();
        
        return application;
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