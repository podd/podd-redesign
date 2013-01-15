/**
 * 
 */
package com.github.podd.resources;

import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class CookieLoginResourceImpl extends AbstractPoddResourceImpl implements LoginResource
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 
     */
    public CookieLoginResourceImpl()
    {
        super();
    }
    
    @Override
    @Get("html")
    public Representation getLoginPageHtml(final Representation entity) throws ResourceException
    {
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        if(user == null)
        {
            this.log.info("In getLoginPageHtml");
            final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
            dataModel.put("contentTemplate", PoddWebConstants.PROPERTY_TEMPLATE_LOGIN);
            
            dataModel.put("shibbolethEnabled", Boolean.valueOf("true"));
            dataModel.put("invalidDomain", Boolean.valueOf("false"));
            
            dataModel.put("pageTitle", "PODD Login Page");
            
            // FIXME: By default use the referrer to populate the redirectTo field internally for
            // use after a successful login
            dataModel.put("referrerRef", this.getRequest().getReferrerRef());
            this.log.info("referrerRef={}", this.getRequest().getReferrerRef());
            
            // Output the base template, with contentTemplate from the dataModel defining the
            // template to use for the content in the body of the page
            return RestletUtils
                    .getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel, MediaType.TEXT_HTML, 
                            this.getPoddApplication().getTemplateConfiguration());
        }
        else
        {
            final Reference referrerRef = this.getRequest().getReferrerRef();
            
            this.log.info("referrerRef={}", referrerRef);
            
            if(referrerRef != null && !referrerRef.equals(this.getRequest().getResourceRef()))
            {
                this.getResponse().redirectSeeOther(referrerRef);
            }
            else
            {
                this.getResponse().redirectSeeOther("location:to:redirect:to:already:logged:in");
            }
            return null;
        }
    }
    
}
