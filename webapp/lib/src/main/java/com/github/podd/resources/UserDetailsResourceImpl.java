/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddRoles;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddRdfConstants;
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
    
    @Get(":html")
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
        
        this.checkAuthentication(action);
        
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
    
    @Get(":rdf|rj|ttl")
    public Representation getUserRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("getUserRdf");
        
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
        
        this.checkAuthentication(action);
        
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final PoddUser poddUser = (PoddUser)realm.findUser(requestedUserIdentifier);
        
        if(poddUser == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "User not found.");
        }
        final Set<Role> roles = realm.findRoles(poddUser);
        
        final Model userInfoModel = this.userToModel(poddUser, roles);
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            Rio.write(userInfoModel, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }
    
    /**
     * Helper method to create a Model containing PoddUser details and Role mappings.
     */
    private Model userToModel(final PoddUser user, final Set<Role> roles)
    {
        final Model userInfoModel = new LinkedHashModel();
        
        final URI userUri = user.getUri();
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                PoddRdfConstants.VF.createLiteral(user.getIdentifier()));
        // Password should not be sent back!
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral(user.getFirstName()));
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral(user.getLastName()));
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USEREMAIL,
                PoddRdfConstants.VF.createLiteral(user.getEmail()));
        
        if(user.getHomePage() != null)
        {
            userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_HOMEPAGE, user.getHomePage());
        }
        
        if(user.getOrganization() != null)
        {
            userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_ORGANIZATION,
                    PoddRdfConstants.VF.createLiteral(user.getOrganization()));
        }
        
        if(user.getOrcid() != null)
        {
            userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_ORCID,
                    PoddRdfConstants.VF.createLiteral(user.getOrcid()));
        }
        
        if (user.getTitle() != null)
        {
            userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_TITLE, PoddRdfConstants.VF.createLiteral(user.getTitle()));
        }
        
        if (user.getPhone() != null)
        {
            userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_PHONE, PoddRdfConstants.VF.createLiteral(user.getPhone()));
        }

        if (user.getAddress() != null)
        {
            userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_ADDRESS, PoddRdfConstants.VF.createLiteral(user.getAddress()));
        }
        
        if (user.getPosition() != null)
        {
            userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_POSITION, PoddRdfConstants.VF.createLiteral(user.getPosition()));
        }
        
        
        this.log.debug("User has {} roles", roles.size());
        final Iterator<Role> iterator = roles.iterator();
        while(iterator.hasNext())
        {
            final Role role = iterator.next();
            final RestletUtilRole roleByName = PoddRoles.getRoleByName(role.getName());
            
            final URI roleMapping =
                    PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
            userInfoModel.add(roleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
            userInfoModel.add(roleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, userUri);
            
            userInfoModel.add(roleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE, roleByName.getURI());
            
            // TODO: include optional object URIs that are mapped to this role
        }
        
        return userInfoModel;
    }
}