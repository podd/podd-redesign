/**
 * 
 */
package com.github.podd.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

import com.github.ansell.restletutils.RestletUtilUser;
import com.github.podd.api.PoddArtifactManager;
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
        
        final String requestedUserIdentifier = (String) this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("requested user: {}", requestedUserIdentifier);

        if(requestedUserIdentifier == null)
        {
            // no identifier specified.
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user to view");
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        PoddAction action = PoddAction.OTHER_USER_READ;
        if (user != null && requestedUserIdentifier.equals(user.getIdentifier()))
        {
            action = PoddAction.CURRENT_USER_READ;
        }
        
        this.checkAuthentication(action, Collections.<URI>emptySet());
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "userDetails.html.ftl");
        dataModel.put("pageTitle", "PODD User Details Page");

        //TODO: Username is firstName + Lastname, not the identifier 
        dataModel.put("authenticatedUsername", user.getIdentifier());
        
        final Map<String, Object> tempUserMap = this.getUserInfoInternal(requestedUserIdentifier);
        
        dataModel.put("requestedUser", tempUserMap);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    // FIXME: get user info and populate data model
    // populating dummy info for test
    private Map<String, Object> getUserInfoInternal(String identifier)
    {
        
        PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        PoddUser user = (PoddUser)realm.findUser(identifier);
        
        this.log.info("Populating user info for {}", user);
        
        final Map<String, Object> tempUserMap = new HashMap<String, Object>();
        tempUserMap.put("userName", user.getIdentifier());
        tempUserMap.put("email", user.getEmail());
        tempUserMap.put("status", user.getUserStatus());
        
        tempUserMap.put("firstName", user.getFirstName());
        tempUserMap.put("lastName", user.getLastName());
        tempUserMap.put("affiliation", user.getOrganization());
        tempUserMap.put("orcid", user.getOrcid());
        tempUserMap.put("homepage", user.getHomePage());
        
        //TODO - these should be persisted in and retrieved from the Repository
        tempUserMap.put("title", "Mr"); 
        tempUserMap.put("phoneNumber", "009988334");
        tempUserMap.put("postalAddress", "88, Some Street, Some Suburb, QLD 4300");
        
        //TODO - get role details
//        final List<String> rolesList = new ArrayList<String>();
        
//        final Map<String, String> roleMap = new HashMap<String, String>();
        Set<Role> roles = realm.findRoles(user);
//        for (Role role: roles)
//        {
//            this.log.info("Adding role {} to map", role.getName());
//            rolesList.add(role.getName());
//        }
        
//        roleMap.put("description", "");
//        roleMap.put("description", "A dummy user account for testing");
        
        tempUserMap.put("repositoryRoleList", roles);
        
        return tempUserMap;
    }
    
}
