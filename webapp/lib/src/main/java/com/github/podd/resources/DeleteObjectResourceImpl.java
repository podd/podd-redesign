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
import java.util.Arrays;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Delete;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.OWLException;

import com.github.podd.exception.ArtifactModifyException;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PublishedArtifactModifyException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

/**
 * Delete an internal PODD object.
 *
 * @author kutila
 *
 */
public class DeleteObjectResourceImpl extends AbstractPoddResourceImpl
{
    /**
     * Accept an HTTP delete request to delete a PODD object. Upon successful deletion, an HTTP 200
     * response with the updated Inferred Ontology ID is sent to the client.
     *
     * @param entity
     * @throws ResourceException
     */
    @Delete
    public Representation deleteObject(final Representation entity, final Variant variant) throws ResourceException
    {
        try
        {
            final String artifactUri = this.getQueryValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
            if(artifactUri == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
            }

            final String versionUri =
                    this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
            if(versionUri == null)
            {
                this.log.error("Artifact Version IRI not submitted");
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact Version IRI not submitted");
            }

            final String objectUri = this.getQueryValue(PoddWebConstants.KEY_OBJECT_IDENTIFIER);
            if(objectUri == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Object to delete not specified in request");
            }

            String cascadeString = this.getQueryValue(PoddWebConstants.KEY_CASCADE);
            if(cascadeString == null)
            {
                cascadeString = "false";
            }
            final boolean cascade = Boolean.valueOf(cascadeString);

            this.checkAuthentication(PoddAction.ARTIFACT_EDIT, PODD.VF.createURI(artifactUri));

            this.log.debug("requesting delete object: {}, {}, {} with cascade {}", artifactUri, versionUri, objectUri,
                    cascade);

            final User user = this.getRequest().getClientInfo().getUser();
            this.log.debug("authenticated user: {}", user);

            final InferredOWLOntologyID updatedOntologyID =
                    this.getPoddArtifactManager().deleteObject(PODD.VF.createURI(artifactUri),
                            PODD.VF.createURI(versionUri), PODD.VF.createURI(objectUri), cascade);

            // - prepare response
            final ByteArrayOutputStream output = new ByteArrayOutputStream(8096);
            final RDFFormat outputFormat =
                    Rio.getWriterFormatForMIMEType(variant.getMediaType().getName(), RDFFormat.RDFXML);
            final Model model =
                    OntologyUtils.ontologyIDsToModel(Arrays.asList(updatedOntologyID), new LinkedHashModel(), false);
            Rio.write(model, output, outputFormat);

            return new ByteArrayRepresentation(output.toByteArray(), MediaType.valueOf(outputFormat
                    .getDefaultMIMEType()));
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact is not managed", e);
        }
        catch(final PublishedArtifactModifyException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Cannot modify published Artifact ", e);
        }
        catch(final ArtifactModifyException e)
        {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Object cannot be deleted", e);
        }
        catch(final PoddException | OpenRDFException | IOException | OWLException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Could not delete artifact due to an internal error", e);
        }
    }

}
