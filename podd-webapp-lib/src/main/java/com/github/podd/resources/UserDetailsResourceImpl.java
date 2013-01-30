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
 * User Details resource
 * 
 * @author kutila
 * 
 */
public class UserDetailsResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get
    public Representation getUserDetailsPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("getUserDetailsHtml");
        
        final String requestedUserIdentifier =
                (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("requesting details of user: {}", requestedUserIdentifier);
        
        if(requestedUserIdentifier == null)
        {
            // no identifier specified.
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user to view");
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // identify needed Action
        PoddAction action = PoddAction.OTHER_USER_READ;
        if(user != null && requestedUserIdentifier.equals(user.getIdentifier()))
        {
            action = PoddAction.CURRENT_USER_READ;
        }
        
        this.checkAuthentication(action, Collections.<URI> emptySet());
        // completed checking authorization
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "userDetails.html.ftl");
        dataModel.put("pageTitle", "PODD User Details Page");
        dataModel.put("authenticatedUsername", user.getIdentifier());
        
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final PoddUser poddUser = (PoddUser)realm.findUser(requestedUserIdentifier);
        
        if(poddUser == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "User not found.");
        }
        else
        {
            this.log.debug("Populating user info for {}", user);
            
            final Map<String, Object> tempUserMap = new HashMap<String, Object>();
            tempUserMap.put("userName", poddUser.getIdentifier());
            tempUserMap.put("email", poddUser.getEmail());
            tempUserMap.put("status", poddUser.getUserStatus());
            
            tempUserMap.put("firstName", poddUser.getFirstName());
            tempUserMap.put("lastName", poddUser.getLastName());
            tempUserMap.put("affiliation", poddUser.getOrganization());
            tempUserMap.put("orcid", poddUser.getOrcid());
            tempUserMap.put("homepage", poddUser.getHomePage());
            
            final Set<Role> roles = realm.findRoles(user);
            tempUserMap.put("repositoryRoleList", roles);
            
            // FIXME: - these should be persisted in and retrieved from the Repository
            tempUserMap.put("title", "Mr");
            tempUserMap.put("phoneNumber", "009988334");
            tempUserMap.put("postalAddress", "88, Some Street, Some Suburb, QLD 4300");
            tempUserMap.put("position", "Some position");
            
            dataModel.put("requestedUser", tempUserMap);
        }
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
}