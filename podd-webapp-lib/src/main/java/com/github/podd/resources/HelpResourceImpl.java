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

import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * Resource for the "help" pages. Does not require authentication.
 * 
 * @author kutila
 * 
 */
public class HelpResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get
    public Representation getAboutPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getHelpPageHtml");
        
        String helpPage = (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_HELP_PAGE_IDENTIFIER);
        this.log.info("requesting help page: {}", helpPage);
        
        if(helpPage == null || helpPage.trim().length() == 0)
        {
            helpPage = "overview";
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "help.html.ftl");
        
        dataModel.put("pageTitle", "PODD Help");
        dataModel.put("content", helpPage);
        
        // FIXME: By default use the referrer to populate the redirectTo field internally for
        // use after a successful login
        dataModel.put("referrerRef", this.getRequest().getReferrerRef());
        this.log.info("referrerRef={}", this.getRequest().getReferrerRef());
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template
        // to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
}
