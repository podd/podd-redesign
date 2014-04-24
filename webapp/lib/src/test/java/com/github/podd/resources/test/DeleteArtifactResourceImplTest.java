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
package com.github.podd.resources.test;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 *
 */
public class DeleteArtifactResourceImplTest extends AbstractResourceImplTest
{
    @Test
    public void testDeleteArtifactBasicRdf() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);

        final ClientResource deleteArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_DELETE));

        try
        {
            deleteArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            deleteArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());

            this.doTestAuthenticatedRequest(deleteArtifactClientResource, Method.DELETE, null,
                    MediaType.APPLICATION_RDF_XML, Status.SUCCESS_NO_CONTENT, AbstractResourceImplTest.WITH_ADMIN);
        }
        finally
        {
            this.releaseClient(deleteArtifactClientResource);
        }

        // verify: try to retrieve deleted artifact
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));

        try
        {
            getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            this.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null, MediaType.APPLICATION_RDF_XML,
                    Status.CLIENT_ERROR_NOT_FOUND, AbstractResourceImplTest.WITH_ADMIN);
            Assert.fail("Should have failed with a NOT_FOUND error");
        }
        catch(final ResourceException e)
        {
            // TODO: Test the exception
        }
        finally
        {
            this.releaseClient(getArtifactClientResource);
        }
    }

}
