/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.resources;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.MetadataPolicy;
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
    /**
     * Return meta-data about an object.
     */
    @Get("rdf|rj|json|ttl")
    public Representation getRdf(final Variant variant) throws ResourceException
    {
        // - object Type (mandatory)
        final String objectType = this.getQuery().getFirstValue(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER, true);
        if(objectType == null)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Type of Object to create not specified");
        }
        
        // - artifact URI (optional)
        final String artifactUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, true);
        
        // - include Do-Not-Display properties (optional, defaults to false)
        final String includeDoNotDisplayPropertiesString =
                this.getQuery().getFirstValue(PoddWebConstants.KEY_INCLUDE_DO_NOT_DISPLAY_PROPERTIES, true);
        final boolean includeDoNotDisplayProperties = Boolean.valueOf(includeDoNotDisplayPropertiesString);
        
        // - metadata policy (optional, default is to exclude sub-properties of poddBase:contains)
        final String metadataPolicyString = this.getQuery().getFirstValue(PoddWebConstants.KEY_METADATA_POLICY, true);
        
        MetadataPolicy containsPropertyPolicy = MetadataPolicy.EXCLUDE_CONTAINS;
        if(metadataPolicyString != null)
        {
            if(metadataPolicyString.equalsIgnoreCase(PoddWebConstants.METADATA_ONLY_CONTAINS))
            {
                containsPropertyPolicy = MetadataPolicy.ONLY_CONTAINS;
            }
            else if(metadataPolicyString.equalsIgnoreCase(PoddWebConstants.METADATA_ALL))
            {
                containsPropertyPolicy = MetadataPolicy.INCLUDE_ALL;
            }
        }
        
        this.log.info("@Get Metadata: {}, {}, {}, {} ({})", objectType, containsPropertyPolicy,
                includeDoNotDisplayProperties, artifactUri, variant.getMediaType().getName());
        
        if(artifactUri == null)
        {
            // looks like adding a new Artifact (ie, a new Project)
            this.checkAuthentication(PoddAction.ARTIFACT_CREATE);
        }
        else
        {
            this.checkAuthentication(PoddAction.ARTIFACT_EDIT, PoddRdfConstants.VF.createURI(artifactUri));
        }
        
        final User user = this.getRequest().getClientInfo().getUser();
        this.log.info("authenticated user: {}", user);
        
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final RDFFormat format = Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.TURTLE);
        try
        {
            InferredOWLOntologyID artifactID = null;
            if(artifactUri != null)
            {
                artifactID = this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
            }
            
            this.getPoddArtifactManager().exportObjectMetadata(PoddRdfConstants.VF.createURI(objectType), output,
                    format, includeDoNotDisplayProperties, containsPropertyPolicy, artifactID);
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given artifact", e);
        }
        catch(OpenRDFException | IOException | PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Could not generate object metadata", e);
        }
        
        return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(format.getDefaultMIMEType()));
    }
    
}
