/**
 * 
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.restlet.PoddAction;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * Search ontology service to search for matching results from an Ontology.
 * 
 * @author kutila
 * 
 */
public class SearchOntologyResourceImpl extends AbstractPoddResourceImpl
{
    
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Get("rdf|rj|json")
    public Representation getRdf(final Variant variant) throws ResourceException
    {
        this.log.info("searchRdf");
        
        final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
        RDFWriter writer = null;
        
        try
        {
            final String searchTerm = this.getQuery().getFirstValue(PoddWebConstants.KEY_SEARCHTERM);
            
            if(searchTerm == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Search term not submitted");
            }
            
            // TODO - artifact URI is NOT needed
            final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
            
            this.log.info("requesting search ({}): {}, {}", variant.getMediaType().getName(), searchTerm, artifactUri);
            
            // TODO - add a new PoddAction to suit the search.
            this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_LIST,
                    Collections.<URI> singleton(PoddRdfConstants.VALUE_FACTORY.createURI(artifactUri)));
            
            final User user = this.getRequest().getClientInfo().getUser();
            this.log.info("authenticated user: {}", user);
            
            // TODO - create a search method in SchemaManager and call it
            final Model results = null;
            
            // - prepare response
            writer =
                    Rio.createWriter(
                            Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML), output);
            output.write("TestResponse".getBytes());
            
        }
        catch(final IOException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Something went wrong :-(", e);
        }
        return new ByteArrayRepresentation(output.toByteArray()// ,
        // MediaType.valueOf(writer.getRDFFormat().getDefaultMIMEType())
        );
    }
    
}
