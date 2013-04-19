/**
 * 
 */
package com.github.podd.restlet.test;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class SearchOntologyResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test successful search for a Platform in RDF
     */
    @Test
    public void testSearchRdf_Case1() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final ClientResource searchClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_SEARCH));
        
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_SEARCHTERM, "PlantS");
        searchClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(searchClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify: response in RDF/json
        Assert.assertNotNull("NULL response body", body);
        // Assert.assertTrue("Response not in RDF format", body.contains("<rdf:RDF"));
        // Assert.assertTrue("Artifact version has not been updated properly",
        // body.contains("artifact:1:version:2"));
        // Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
        // Assert.assertTrue("Inferred version not in response", body.contains("inferredVersion"));
        
        // verify: search results are as expected
    }
    
    /**
     */
    @Ignore
    @Test
    public void testSearchRdf_Case2() throws Exception
    {
    }
    
    /**
     */
    @Ignore
    @Test
    public void testSearchTurtle() throws Exception
    {
    }
    
    /**
     */
    @Ignore
    @Test
    public void testErrorSearchRdf_Cause1() throws Exception
    {
    }
    
    @Ignore
    @Test
    public void testErrorSearchRdf_Cause2() throws Exception
    {
    }
    
}
