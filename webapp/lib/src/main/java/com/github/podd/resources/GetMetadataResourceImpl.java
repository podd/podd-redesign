package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * Resource to create new PODD object.
 * 
 * @author kutila
 */
public class GetMetadataResourceImpl extends AbstractPoddResourceImpl
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /** Constructor */
    public GetMetadataResourceImpl()
    {
        super();
    }
    
    /**
     * Return meta-data about an object.
     */
    @Get("rdf|rj|json|ttl")
    public Representation getRdf(final Variant variant) throws ResourceException
    {
        // - object Type (mandatory)
        final String objectType = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER);
        if(objectType == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Type of Object to create not specified");
        }
        
        // - artifact URI (optional)
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
        
        // - include Do-Not-Display properties (optional, defaults to false)
        final String includeDoNotDisplayPropertiesString =
                this.getQuery().getFirstValue(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, true);
        final boolean includeDoNotDisplayProperties = Boolean.valueOf(includeDoNotDisplayPropertiesString);
        
        this.log.info("@Get Metadata: {} ({})", objectType, variant.getMediaType().getName());
        
        this.checkAuthentication(PoddAction.ARTIFACT_CREATE, Collections.<URI> emptySet());
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        try
        {
            InferredOWLOntologyID artifactID = null;
            if(artifactUri != null)
            {
                artifactID = this.getPoddArtifactManager().getArtifactByIRI(IRI.create(artifactUri));
            }
            
            this.getPoddArtifactManager().exportObjectMetadata(PoddRdfConstants.VF.createURI(objectType),
                    output, RDFFormat.forMIMEType(variant.getMediaType().getName(), RDFFormat.TURTLE),
                    includeDoNotDisplayProperties, artifactID);
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        catch(OpenRDFException | IOException | PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not generate object metadata", e);
        }
        
        return new ByteArrayRepresentation(output.toByteArray());
    }
    
}
