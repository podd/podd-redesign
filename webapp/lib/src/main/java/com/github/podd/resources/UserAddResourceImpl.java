/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDFS;
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
import org.semanticweb.owlapi.model.OWLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
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
  
    /**
     * Handle an HTTP POST request submitting RDF data to update an existing artifact
     */
    @Post("rdf|rj|json|ttl")
    public Representation addUserRdf(final Representation entity, final Variant variant)
        throws ResourceException
    {

        // TODO - retrieve from incoming query
        String identifier = "kutila@gmail.com";
        String password = "password";
        String firstName = "Kutila";
        String lastName = "G";
        String email = identifier;
        URI homePage = PoddRdfConstants.VF.createURI("http://sites.google.com/site/kutila");
        String organization = "University of Queensland";
        String orcidID = "orcid-rsch-uq-kg";

        // - add the new User to Realm
        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        
        final PoddUser newUser =
                new PoddUser(identifier, password.toCharArray(), firstName, lastName, email, PoddUserStatus.ACTIVE,
                        homePage, organization, orcidID);
        final URI newUserUri = nextRealm.addUser(newUser);
        
        // - map Roles for the new User
        nextRealm.map(newUser, PoddRoles.ADMIN.getRole());
        nextRealm.map(newUser, PoddRoles.AUTHENTICATED.getRole());
        
        final URI testArtifactUri = PoddRdfConstants.VF.createURI("http://purl.org/podd/ns/artifact/artifact89");
        nextRealm.map(newUser, PoddRoles.PROJECT_ADMIN.getRole(), testArtifactUri);
        
        // - prepare response
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        final RDFFormat outputFormat = Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML); 
        try
        {
            final Model model = new LinkedHashModel();
            // TODO - add user and set response
            model.add(newUserUri, RDFS.LABEL, PoddRdfConstants.VF.createLiteral(newUser.getIdentifier()));
            
            // - write the artifact ID into response
            Rio.write(model, output, outputFormat);
        }
//        catch(final PoddException e)
//        {
//            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response", e);
//        }
        catch(OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }
    
}