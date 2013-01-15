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

/**
 * 
 * Attempt at serving a simple non-authenticated page
 * 
 * @author kutila
 * 
 */
public class AboutResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get
    public Representation getAboutPageHtml(final Representation entity) throws ResourceException
    {
        // Enable the following to test authenticated access
        //this.getPoddApplication().authenticate(PoddAction.ROLE_EDIT, getRequest(), getResponse());
        this.log.info("getAboutPageHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        this.log.info("In getAboutPageHtml");
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "about.html.ftl");
        
        dataModel.put("pageTitle", "PODD About Page");
        
        // FIXME: By default use the referrer to populate the redirectTo field internally for
        // use after a successful login
        dataModel.put("referrerRef", this.getRequest().getReferrerRef());
        this.log.info("referrerRef={}", this.getRequest().getReferrerRef());
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template
        // to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation("poddBase.html.ftl", dataModel, MediaType.TEXT_HTML, this
                .getPoddApplication().getTemplateConfiguration());
    }
    
}
