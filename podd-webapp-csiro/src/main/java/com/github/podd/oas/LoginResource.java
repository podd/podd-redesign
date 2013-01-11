package com.github.podd.oas;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

/**
 * Static login page resource which accepts HTTP GET to return a form if the user is not
 * authenticated, or redirect them if they are authenticated.
 * 
 * HTTP GET returns a static login form
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface LoginResource
{
    /**
     * Fetch a form that can be used in an HTML application to login.
     * 
     * @param entity
     * @return
     * @throws ResourceException
     */
    @Get("html")
    Representation getLoginPageHtml(Representation entity) throws ResourceException;
    
}