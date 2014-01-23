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
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class ListArtifactsResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test authenticated access to list Artifacts in HTML with multiple loaded artifacts where all
     * of the artifacts are unpublished, but also asking for published artifacts.
     */
    @Test
    public void testListArtifactsHtmlEmptyAllUnpublishedWithPublished() throws Exception
    {
        final ClientResource listArtifactsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_LIST));
        
        try
        {
            listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_PUBLISHED, "true");
            listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_UNPUBLISHED, "true");
            
            // Representation results =
            // listArtifactsClientResource.get(MediaType.TEXT_HTML);
            final Representation results =
                    this.doTestAuthenticatedRequest(listArtifactsClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = this.getText(results);
            
            // verify:
            // System.out.println("results:" + body);
            Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
            
            Assert.assertTrue("Page did not contain no artifacts message", body.contains("No projects found"));
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(listArtifactsClientResource);
        }
    }
    
    /**
     * Test authenticated access to list Artifacts in HTML with multiple loaded artifacts where all
     * of the artifacts are unpublished, but also asking for published artifacts.
     */
    @Test
    public void testListArtifactsHtmlMultipleAllUnpublishedWithPublished() throws Exception
    {
        // prepare: add two artifacts
        final String artifactUri1 = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        final String artifactUri2 = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_PROJECT_2);
        
        final ClientResource listArtifactsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_LIST));
        try
        {
            listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_PUBLISHED, "true");
            listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_UNPUBLISHED, "true");
            
            // Representation results =
            // listArtifactsClientResource.get(MediaType.TEXT_HTML);
            final Representation results =
                    this.doTestAuthenticatedRequest(listArtifactsClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = this.getText(results);
            
            // verify:
            // System.out.println("results:" + body);
            Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
            Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
            
            Assert.assertTrue("Missing heading on page - Artifacts Listing", body.contains("Projects Listing"));
            Assert.assertTrue("Missng artifact 1 URI on page", body.contains(artifactUri1));
            Assert.assertTrue("Missng artifact 2 URI on page", body.contains(artifactUri2));
            
            Assert.assertFalse("Page contained no artifacts message", body.contains("No projects found"));
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(listArtifactsClientResource);
        }
    }
    
    /**
     * Test authenticated access to list Artifacts in HTML with multiple loaded artifacts where all
     * of the artifacts are unpublished, but also asking for published artifacts.
     */
    @Test
    public void testListArtifactsRdfJsonEmptyAllUnpublishedWithPublished() throws Exception
    {
        final ClientResource listArtifactsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_LIST));
        try
        {
            listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_PUBLISHED, "true");
            listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_UNPUBLISHED, "true");
            
            // Representation results =
            // listArtifactsClientResource.get(MediaType.TEXT_HTML);
            final Representation results =
                    this.doTestAuthenticatedRequest(listArtifactsClientResource, Method.GET, null,
                            RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK,
                            AbstractResourceImplTest.WITH_ADMIN);
            
            // verify:
            this.assertRdf(results, RDFFormat.RDFJSON, 0);
        }
        finally
        {
            this.releaseClient(listArtifactsClientResource);
        }
    }
    
    /**
     * Test authenticated access to list Artifacts in HTML with multiple loaded artifacts where all
     * of the artifacts are unpublished, but also asking for published artifacts.
     */
    @Test
    public void testListArtifactsRdfJsonMultipleAllUnpublishedWithPublished() throws Exception
    {
        // prepare: add two artifacts
        final String artifactUriString1 = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        final String artifactUriString2 = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_PROJECT_2);
        final URI artifactUri1 = this.vf.createURI(artifactUriString1);
        final URI artifactUri2 = this.vf.createURI(artifactUriString2);
        final ClientResource listArtifactsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_LIST));
        
        try
        {
            listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_PUBLISHED, "true");
            listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_UNPUBLISHED, "true");
            
            // Representation results =
            // listArtifactsClientResource.get(MediaType.TEXT_HTML);
            final Representation results =
                    this.doTestAuthenticatedRequest(listArtifactsClientResource, Method.GET, null,
                            RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK,
                            AbstractResourceImplTest.WITH_ADMIN);
            
            // verify:
            final Model model = this.assertRdf(results, RDFFormat.RDFJSON, 17);
            
            Assert.assertTrue(model.contains(artifactUri1, PODD.PODD_BASE_HAS_TOP_OBJECT, null));
            Assert.assertTrue(model.contains(artifactUri2, PODD.PODD_BASE_HAS_TOP_OBJECT, null));
        }
        finally
        {
            this.releaseClient(listArtifactsClientResource);
        }
    }
    
}
