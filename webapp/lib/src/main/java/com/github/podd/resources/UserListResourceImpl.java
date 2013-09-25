/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ansell.restletutils.RestletUtilUser;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.restlet.RestletUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * Service to list Users.
 * 
 * @author kutila
 */
public class UserListResourceImpl extends AbstractUserResourceImpl
{
    /**
     * FIXME: incomplete implementation
     * 
     * Display the User List HTML page
     */
    @Get(":html")
    public Representation getUsersHtml(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("getUsersHtml");
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        this.checkAuthentication(PoddAction.OTHER_USER_READ);
        
        final Map<String, Object> dataModel = RestletUtils.getBaseDataModel(this.getRequest());
        dataModel.put("contentTemplate", "admin_listUsers.html.ftl");
        dataModel.put("pageTitle", "List Users");
        
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final List<PoddUser> users = realm.getUsers();
        dataModel.put("userObjectList", users);
        
        dataModel.put("statusList", PoddUserStatus.values());
        dataModel.put("roleObjectList", PoddRoles.values());
        
        // Output the base template, with contentTemplate from the dataModel defining the
        // template to use for the content in the body of the page
        return RestletUtils.getHtmlRepresentation(PoddWebConstants.PROPERTY_TEMPLATE_BASE, dataModel,
                MediaType.TEXT_HTML, this.getPoddApplication().getTemplateConfiguration());
    }
    
    /**
     * Get a list of PODD Users
     */
    @Get(":rdf|rj|json|ttl")
    public Representation getUsersRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("getUsersRdf");
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        this.checkAuthentication(PoddAction.OTHER_USER_READ);
        
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final Model resultModel = this.userListToModel(realm.getUsers());
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat =
                Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
        try
        {
            Rio.write(resultModel, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response", e);
        }
        catch(final UnsupportedRDFormatException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not parse input format", e);
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }
    
    /**
     * Convert the User Details into a Model. Role information is not included.
     * 
     * @param users
     * @return
     */
    private Model userListToModel(final List<PoddUser> users)
    {
        final Model model = new LinkedHashModel();
        for(final PoddUser user : users)
        {
            user.toModel(model, false);
        }
        
        return model;
    }
    
}
