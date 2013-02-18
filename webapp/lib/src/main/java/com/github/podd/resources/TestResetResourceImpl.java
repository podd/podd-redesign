/**
 * 
 */
package com.github.podd.resources;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddWebServiceApplication;
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
    
    private PoddWebServiceApplication application;
    
    /**
     */
    public TestResetResourceImpl()
    {
        super();
    }
    
    public TestResetResourceImpl(final PoddWebServiceApplication application)
    {
        this.application = application;
    }
    
    @Get
    public Representation uploadArtifactFile(final Representation entity) throws ResourceException
    {
        // this.checkAuthentication(PoddAction.ROLE_EDIT);
        
        this.log.info("========== Empty reset called ==========");
        
        // TODO: the reset logic should come here
        // ApplicationUtils.setupApplication(this.application, this.application.getContext());
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "index.html.ftl");
        dataModel.put("pageTitle", "PODD has been reset");
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
}
