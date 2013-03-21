/**
 * 
 */
package com.github.podd.resources;

import java.util.Map;

import org.openrdf.OpenRDFException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.ApplicationUtils;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.PoddWebServiceApplicationImpl;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * Resets an application using ApplicationUtils.setupApplication.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class TestResetResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     */
    public TestResetResourceImpl()
    {
        super();
    }
    
    @Get
    public Representation reset() throws ResourceException
    {
        this.log.info("========== Reset called ==========");
        try
        {
            ApplicationUtils.setupApplication(this.getPoddApplication(), this.getPoddApplication().getContext());
        }
        catch(OpenRDFException e)
        {
            this.log.error("Could not reset application", e);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not reset application", e);
        }
        this.log.info("========== Reset complete ==========");
        
        this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
        
        return null;
    }
    
}
