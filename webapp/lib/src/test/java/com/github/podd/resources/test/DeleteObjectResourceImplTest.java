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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class DeleteObjectResourceImplTest extends AbstractResourceImplTest
{
    @Test
    public void testDeleteObjectBasicRdf() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        final String objectToDelete = "http://purl.org/podd/basic-2-20130206/artifact:1#publication45";
        
        final ClientResource deleteObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_OBJECT_DELETE));
        
        deleteObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                .getOntologyIRI().toString());
        deleteObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                .getVersionIRI().toString());
        deleteObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectToDelete);
        deleteObjectClientResource.addQueryParameter(PoddWebConstants.KEY_CASCADE, Boolean.toString(false));
        
        Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(deleteObjectClientResource, Method.DELETE, null,
                        MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: response contains updated artifact's ID
        final String updatedArtifactDetails = getText(results);
        Assert.assertTrue("Artifact version has not been updated properly",
                updatedArtifactDetails.contains("artifact:1:version:2"));
        
        // verify: retrieve artifact and check deleted object's not present
        final Model retrievedArtifact = this.getArtifact(artifactID.getOntologyIRI().toString(), 81);
        final URI objectToDeleteUri = PoddRdfConstants.VF.createURI(objectToDelete);
        Assert.assertTrue("Object not deleted", retrievedArtifact.filter(objectToDeleteUri, null, null).isEmpty());
        Assert.assertTrue("Object not deleted", retrievedArtifact.filter(null, null, objectToDeleteUri).isEmpty());
    }
    
    private Model getArtifact(final String artifactUri, final int expectedStatementCount) throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        
        final Representation getArtifactResult =
                RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model model =
                this.assertRdf(new StringReader(getText(getArtifactResult)), RDFFormat.RDFXML, expectedStatementCount);
        
        return model;
    }
}
