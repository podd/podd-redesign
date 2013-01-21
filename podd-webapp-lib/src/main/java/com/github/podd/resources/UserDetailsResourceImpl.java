/**
 * 
 */
package com.github.podd.resources;

import java.util.HashMap;
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
        this.checkAuthentication(PoddAction.USER_READ);
        
        this.log.info("getUserDetailsHtml");
        final User user = this.getRequest().getClientInfo().getUser();
        
        this.log.info("authenticated user: {}", user);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "userDetails.html.ftl");
        dataModel.put("pageTitle", "PODD User Details Page");
        
        //TODO: Username is firstName + Lastname, not the identifier 
        dataModel.put("authenticatedUsername", user.getIdentifier());
        
        final Map<String, Object> tempUserMap = this.getUserInfoInternal();
        
        dataModel.put("requestedUser", tempUserMap);
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    // FIXME: get user info and populate data model
    // populating dummy info for test
    private Map<String, Object> getUserInfoInternal()
    {
        final Map<String, Object> tempUserMap = new HashMap<String, Object>();
        tempUserMap.put("userName", "john.test");
        tempUserMap.put("email", "john@example.com");
        tempUserMap.put("status", "active");
        
        final Map<String, String> roleMap = new HashMap<String, String>();
        roleMap.put("description", "A dummy user account for testing");
        tempUserMap.put("repositoryRole", roleMap);
        
        tempUserMap.put("title", "Mr");
        tempUserMap.put("firstName", "John");
        tempUserMap.put("lastName", "Test");
        tempUserMap.put("affiliation", "UQ");
        tempUserMap.put("position", "Tester");
        tempUserMap.put("phoneNumber", "009988334");
        tempUserMap.put("postalAddress", "0, Some Street, Some suburb, QLD 4300");
        tempUserMap.put("homepage", "http://john.test.com");
        
        return tempUserMap;
    }
    
}
