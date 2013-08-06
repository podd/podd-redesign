/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
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
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * @author kutila
 */
public class UserRolesResourceImpl extends AbstractPoddResourceImpl
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    //FIXME - GET html to display Role specification page
    
    
    /**
     * Handle an HTTP POST request submitting RDF data to update (i.e. map/unmap) a PoddUser's Roles
     */
    @Post("rdf|rj|json|ttl")
    public Representation editUserRolesRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("editUserRolesRdf");
        
        final String userIdentifier =
                (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_USER_IDENTIFIER);
        this.log.info("updating Roles of user: {}", userIdentifier);
        
        if(userIdentifier == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Did not specify user to update Roles");
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);

        // check authentication first
        PoddAction action = PoddAction.OTHER_USER_EDIT;
        if(user != null && userIdentifier.equals(user.getIdentifier()))
        {
            action = PoddAction.CURRENT_USER_EDIT;
        }
        this.checkAuthentication(action);        
        
        final PoddSesameRealm nextRealm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        
        final RestletUtilUser restletUserToUpdate = nextRealm.findUser(userIdentifier);
        if (restletUserToUpdate == null || !(restletUserToUpdate instanceof PoddUser))
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "User not found");
        }
        final PoddUser poddUser = (PoddUser) restletUserToUpdate;
        
        try
        {
            // - construct Model from input RDF content
            final InputStream inputStream = entity.getStream();
            final RDFFormat inputFormat =
                    Rio.getParserFormatForMIMEType(entity.getMediaType().getName(), RDFFormat.RDFXML);
            final Model incomingRoles = Rio.parse(inputStream, "", inputFormat);

            // - remove old Role Mappings in Realm
            final Set<Role> oldRoles = nextRealm.findRoles(poddUser);
            for(Iterator<Role> iterator1 = oldRoles.iterator(); iterator1.hasNext();)
            {
                final Role role = iterator1.next();
                nextRealm.unmap(poddUser, role);
                this.log.info("**** User [{}] unmapped from Role [{}]", poddUser.getIdentifier(), role.getName());
            }
            
            // - map new Roles for the User
            for(Iterator<Resource> iterator2 =
                    incomingRoles.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING).subjects().iterator(); iterator2
                    .hasNext();)
            {
                final Resource mappingUri = iterator2.next();
                
                final URI roleUri =
                        incomingRoles.filter(mappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objectURI();
                final RestletUtilRole role = PoddRoles.getRoleByUri(roleUri);
                
                final URI mappedObject =
                        incomingRoles.filter(mappingUri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, null).objectURI();
                
                this.log.debug("Mapping <{}> to Role <{}> with Optional Object <{}>", poddUser.getIdentifier(),
                        role.getName(), mappedObject);
                if(mappedObject != null)
                {
                    nextRealm.map(poddUser, role.getRole(), mappedObject);
                }
                else
                {
                    nextRealm.map(poddUser, role.getRole());
                }
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
            model.add(poddUser.getUri(), SesameRealmConstants.OAS_USERIDENTIFIER,
                    PoddRdfConstants.VF.createLiteral(poddUser.getIdentifier()));
            Rio.write(model, output, outputFormat);
        }
        catch(final OpenRDFException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not create response");
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat.getDefaultMIMEType()));
    }

    
}
