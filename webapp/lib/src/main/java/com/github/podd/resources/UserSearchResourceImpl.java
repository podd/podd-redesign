/**
 *
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDFS;
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

import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.podd.restlet.PoddAction;
import com.github.podd.restlet.PoddSesameRealm;
import com.github.podd.restlet.PoddWebServiceApplication;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddUser;
import com.github.podd.utils.PoddWebConstants;

/**
 * RDF Service to search Users.
 *
 * @author kutila
 */
public class UserSearchResourceImpl extends AbstractUserResourceImpl
{
    /**
     * Search for PODD Users
     */
    @Get(":rdf|rj|json|ttl")
    public Representation searchUsersRdf(final Representation entity, final Variant variant) throws ResourceException
    {
        this.log.info("searchUsersRdf");
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        this.checkAuthentication(PoddAction.OTHER_USER_SEARCH);
        
        // - get input search term (mandatory)
        final String searchTerm = this.getQuery().getFirstValue(PoddWebConstants.KEY_SEARCHTERM, true);
        if(searchTerm == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Search term not submitted");
        }
        
        // - search for matching users in Realm
        final PoddSesameRealm realm = ((PoddWebServiceApplication)this.getApplication()).getRealm();
        final List<PoddUser> resultList = realm.searchUser(searchTerm, null, false, -1, 0);
        
        // - convert results into a Model for sending back
        final Model resultModel = new LinkedHashModel();
        for(final PoddUser resultUser : resultList)
        {
            final String label = resultUser.getUserLabel();
            resultModel.add(resultUser.getUri(), RDFS.LABEL, PODD.VF.createLiteral(label));
            resultModel.add(resultUser.getUri(), SesameRealmConstants.OAS_USERIDENTIFIER,
                    PODD.VF.createLiteral(resultUser.getIdentifier()));
        }
        
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
    
}
