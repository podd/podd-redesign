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

import java.io.IOException;

import org.openrdf.OpenRDFException;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Delete;
import org.restlet.resource.ResourceException;
import org.restlet.security.User;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;

import com.github.podd.exception.PoddException;
import com.github.podd.restlet.PoddAction;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
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
     * Accept an HTTP delete request to delete a PODD object. Upon successful deletion,
     * an HTTP 204 (No Content) response is sent to the client.
     * 
     * @param entity
     * @throws ResourceException
     */
    @Delete
    public void deleteObject(final Representation entity) throws ResourceException
    {
        try
        {
            final String artifactUri = this.getQueryValue(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER);
            if(artifactUri == null)
            {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Artifact IRI not submitted");
            }

            final String versionUri = this.getQuery().getFirstValue(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, true);
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
            if (cascadeString == null)
            {
                cascadeString = "false";
            }
            final boolean cascade = Boolean.valueOf(cascadeString);
            
            this.checkAuthentication(PoddAction.ARTIFACT_EDIT, PoddRdfConstants.VF.createURI(artifactUri));
            
            this.log.info("requesting delete object: {}, {}, {} with cascade {}", artifactUri, versionUri, objectUri,
                    cascade);
            
            final User user = this.getRequest().getClientInfo().getUser();
            this.log.info("authenticated user: {}", user);
            
              
            final InferredOWLOntologyID updatedOntologyID = this.getPoddArtifactManager().deleteObject(artifactUri, versionUri, objectUri, cascade);
            this.getPoddArtifactManager().getArtifact(IRI.create(artifactUri));
            
            this.getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
            // TODO - send updated artifact ID in response. Handle exception to indicate delete failed and return that separately
            //throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Could not delete artifact");
        }
        catch(final PoddException | OpenRDFException | IOException | OWLException e)
        {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Could not delete artifact due to an internal error", e);
        }
    }

}
