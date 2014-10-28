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

import org.openrdf.OpenRDFException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.exception.UnmanagedSchemaOntologyIDException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 *
 * Get a schema ontology from PODD.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class GetSchemaResourceImpl extends AbstractPoddResourceImpl
{
    
    @Get(":rdf|rj|json|ttl")
    public Representation getSchemaRdf(final Variant variant) throws ResourceException
    {
        this.log.debug("getSchemaRdf");
        
        try
        {
            String schemaString = this.getQuery().getFirstValue(PoddWebConstants.KEY_SCHEMA_URI, true);
            
            final String versionString = this.getQuery().getFirstValue(PoddWebConstants.KEY_SCHEMA_VERSION_URI, true);
            
            final String schemaPath = (String)this.getRequest().getAttributes().get(PoddWebConstants.KEY_SCHEMA_PATH);
            
            // If they didn't specify anything in query parameters, look at the
            // path for a backup
            if(schemaString == null && versionString == null)
            {
                if(schemaPath != null)
                {
                    schemaString = PoddWebConstants.PURL_ORG_PODD_NS + schemaPath;
                }
                
                if(schemaString == null)
                {
                    this.log.error("Schema URI not submitted");
                    throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Schema URI not submitted");
                }
            }
            
            this.log.debug("requesting get schema ({}): {} {} {}", variant.getMediaType().getName(), schemaString,
                    versionString, schemaPath);
            
            // TODO: Is authentication required for resolving schemas?
            // this.checkAuthentication(PoddAction.UNPUBLISHED_ARTIFACT_READ,
            // PoddRdfConstants.VF.createURI(schemaString));
            // completed checking authorization
            
            // final User user = this.getRequest().getClientInfo().getUser();
            // this.log.debug("authenticated user: {}", user);
            
            InferredOWLOntologyID ontologyID = null;
            
            if(versionString == null)
            {
                ontologyID = this.getPoddSchemaManager().getCurrentSchemaOntologyVersion(IRI.create(schemaString));
            }
            else if(schemaString == null)
            {
                ontologyID = this.getPoddSchemaManager().getSchemaOntologyVersion(IRI.create(versionString));
            }
            else
            {
                // Fetch the exact ontology
                ontologyID =
                        this.getPoddSchemaManager().getSchemaOntologyID(
                                new OWLOntologyID(IRI.create(schemaString), IRI.create(versionString)));
            }
            
            if(ontologyID == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given schema or version");
            }
            
            final String includeInferredString =
                    this.getRequest().getResourceRef().getQueryAsForm()
                            .getFirstValue(PoddWebConstants.KEY_INCLUDE_INFERRED, true);
            final boolean includeInferred = Boolean.valueOf(includeInferredString);
            
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            this.getPoddSchemaManager()
                    .downloadSchemaOntology(ontologyID, stream,
                            Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML),
                            includeInferred);
            return new ByteArrayRepresentation(stream.toByteArray());
        }
        catch(final UnmanagedSchemaIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, "Could not find the given schema or version", e);
        }
        catch(final UnmanagedSchemaOntologyIDException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND,
                    "Could not find the given schema and version combination", e);
        }
        catch(OpenRDFException | PoddException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Failed to export schema", e);
        }
    }
}
