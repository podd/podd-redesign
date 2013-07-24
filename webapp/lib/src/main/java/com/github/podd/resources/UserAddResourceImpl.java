/**
 * 
 */
package com.github.podd.resources;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * User Add resource
 * 
 * @author kutila
 * 
 */
public class UserAddResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get
    public Representation getUserDetailsPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("addUserHtml");
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // identify needed Action
        PoddAction action = PoddAction.USER_CREATE;
        
        this.checkAuthentication(action, Collections.<URI> emptySet());
        // completed checking authorization
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "admin_createUser.html.ftl");
        dataModel.put("pageTitle", "Add PODD User Page");
        dataModel.put("authenticatedUsername", user.getIdentifier());
        
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        
        //FIXME - complete necessary info for Add User page
        
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
}