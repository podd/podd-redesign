/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
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
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.RestletUtilUser;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddRoles;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * User Edit resource
 * 
 * @author kutila
 * 
 */
public class UserEditResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * Handle an HTTP GET request to display the Edit User page in HTML
     * 
     * FIXME: incomplete, initial untested code
     */
    @Get
    public Representation getUserEditPageHtml(final Representation entity) throws ResourceException
    {
        this.log.info("editUserHtml");

        final String requestedUserIdentifier =
                (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("requesting edit user: {}", requestedUserIdentifier);
        
        if(requestedUserIdentifier == null)
        {
            // no identifier specified.
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user to edit");
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        // TODO: identify needed Action
        PoddAction action = PoddAction.OTHER_USER_READ;
        if(user != null && requestedUserIdentifier.equals(user.getIdentifier()))
        {
            action = PoddAction.CURRENT_USER_READ;
        }
        this.checkAuthentication(action);
        
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "editUser.html.ftl");
        dataModel.put("pageTitle", "Edit PODD User Page");
        dataModel.put("title", "Edit User");
        dataModel.put("authenticatedUsername", user.getIdentifier());
        
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final PoddUser poddUser = (PoddUser)realm.findUser(requestedUserIdentifier);
        
        if(poddUser == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "User not found.");
        }
        else
        {
            dataModel.put("requestedUser", poddUser);
            
            final Set<Role> roles = realm.findRoles(poddUser);
            dataModel.put("repositoryRoleList", roles);
        }
        
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    /**
     * Handle an HTTP POST request submitting RDF data to edit an existing PoddUser.
     * 
     * FIXME: Implement me, just copied code from addUserRdf
     */
    @Post("rdf|rj|json|ttl")
    public Representation editUserRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        // check authentication first
        this.checkAuthentication(PoddAction.USER_CREATE);
        
        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        
        URI newUserUri = null;
        PoddUser modifiedUser = null;
        try
        {
            // - get input stream with RDF content
            final InputStream inputStream = entity.getStream();
            final RDFFormat inputFormat =
                    Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
            final Model modifiedUserModel = Rio.parse(inputStream, "", inputFormat);
            
            // - create new PoddUser and add to Realm
            modifiedUser = this.modelToUser(modifiedUserModel);
            
            // TODO: better to throw a specific exception (e.g. DuplicateUserException) that could
            // be caught further below
            try
            {
                newUserUri = nextRealm.addUser(modifiedUser);
            }
            catch(RuntimeException e)
            {
                throw new ResourceException(Status.CLIENT_ERROR_CONFLICT, e);
            }
            
            this.log.debug("Added new User <{}>", modifiedUser.getIdentifier());
            
            // - map Roles for the new User
            final Iterator<Resource> iterator =
                    modifiedUserModel.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING).subjects().iterator();
            while(iterator.hasNext())
            {
                final Resource mappingUri = iterator.next();
                
                final URI roleUri =
                        modifiedUserModel.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objectURI();
                final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
                
                final URI mappedObject =
                        modifiedUserModel.filter(mappingUri, PoddWebConstants.PODD_ROLEMAPPEDOBJECT, null).objectURI();
                
                this.log.debug("Mapping <{}> to Role <{}> with Optional Object <{}>", modifiedUser.getIdentifier(),
                        role.getName(), mappedObject);
                if(mappedObject != null)
                {
                    nextRealm.map(modifiedUser, role.getRole(), mappedObject);
                }
                else
                {
                    nextRealm.map(modifiedUser, role.getRole());
                }
            }
            
            // - check the User was successfully added to the Realm
            final RestletUtilUser findUser = nextRealm.findUser(modifiedUser.getIdentifier());
            if(findUser == null)
            {
                throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to add user");
            }
            
        }
        catch(final IOException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "There was a problem with the input", e);
        }
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            final Model model = new LinkedHashModel();
            model.add(newUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                    PoddRdfConstants.VF.createLiteral(modifiedUser.getIdentifier()));
            Rio.write(model, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }
    
    /**
     * Helper method to construct a {@link PoddUser} from information in the given {@link Model}.
     * 
     * @param model
     * @return
     * @throws ResourceException
     *             if mandatory data is missing.
     */
    private PoddUser modelToUser(final Model model)
    {
        final String identifier = model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString();
        if(identifier == null || identifier.trim().length() == 0)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User Identifier cannot be empty");
        }
        
        final String password = model.filter(null, SesameRealmConstants.OAS_USERSECRET, null).objectString();
        if(password == null || password.trim().length() == 0)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User Password cannot be empty");
        }
        
        final String firstName = model.filter(null, SesameRealmConstants.OAS_USERFIRSTNAME, null).objectString();
        final String lastName = model.filter(null, SesameRealmConstants.OAS_USERLASTNAME, null).objectString();
        // PODD-specific requirement. First/Last names are mandatory.
        if(firstName == null || lastName == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User First/Last name cannot be empty");
        }
        
        // PODD-specific requirement. Email has to be present and equal to the user Identifier.
        final String email = model.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).objectString();
        if(email == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User Email cannot be empty");
        }
        else if(!email.equalsIgnoreCase(identifier))
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "User Email has to be the same as User Identifier");
        }
        
        final URI homePage = model.filter(null, PoddRdfConstants.PODD_USER_HOMEPAGE, null).objectURI();
        final String organization = model.filter(null, PoddRdfConstants.PODD_USER_ORGANIZATION, null).objectString();
        final String orcidID = model.filter(null, PoddRdfConstants.PODD_USER_ORCID, null).objectString();
        final String title = model.filter(null, PoddRdfConstants.PODD_USER_TITLE, null).objectString();
        final String phone = model.filter(null, PoddRdfConstants.PODD_USER_PHONE, null).objectString();
        final String address = model.filter(null, PoddRdfConstants.PODD_USER_ADDRESS, null).objectString();
        final String position = model.filter(null, PoddRdfConstants.PODD_USER_POSITION, null).objectString();
        
        final PoddUser user =
                new PoddUser(identifier, password.toCharArray(), firstName, lastName, email, PoddUserStatus.ACTIVE,
                        homePage, organization, orcidID, title, phone, address, position);
        
        return user;
    }
}