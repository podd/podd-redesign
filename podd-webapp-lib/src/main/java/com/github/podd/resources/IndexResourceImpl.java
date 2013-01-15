/**
 * 
 */
package com.github.podd.resources;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * Attempt at serving a simple non-authenticated page
 * 
 * @author kutila
 * 
 */
public class IndexResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get
    public Representation getAboutPageHtml(final Representation entity) throws ResourceException
    {
        // Enable the following to test authenticated access
        //this.getPoddApplication().authenticate(PoddAction.ROLE_EDIT, getRequest(), getResponse());
        this.log.info("getIndexPageHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "index.html.ftl");
        
        dataModel.put("pageTitle", "PODD Index Page");
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE,
                dataModel, MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
}
