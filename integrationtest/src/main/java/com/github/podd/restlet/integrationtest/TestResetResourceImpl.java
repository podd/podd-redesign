/**
 * 
 */
package com.github.podd.restlet.integrationtest;

import org.openrdf.OpenRDFException;
import org.openrdf.model.impl.LinkedHashModel;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.restlet.PoddWebServiceApplication;

/**
 * Resets an application using ApplicationUtils.setupApplication.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class TestResetResourceImpl extends Restlet
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private PoddWebServiceApplication application;
    
    /**
     */
    public TestResetResourceImpl()
    {
        super();
    }
    
    public TestResetResourceImpl(final PoddWebServiceApplication nextApplication)
    {
        this.application = nextApplication;
    }
    
    @Override
    public void handle(final Request request, final Response response)
    {
        super.handle(request, response);
        
        this.log.info("========== Reset called ==========");
        try
        {
            // Reset the aliases configuration to that it will be regenerated each time
            this.application.setAliasesConfiguration(new LinkedHashModel());
            ApplicationUtils.setupApplication(this.application, this.application.getContext());
        }
        catch(final OpenRDFException e)
        {
            this.log.error("Could not reset application", e);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not reset application", e);
        }
        this.log.info("========== Reset complete ==========");
        
        response.setStatus(Status.SUCCESS_NO_CONTENT);
    }
    
}
