/**
 * 
 */
package com.github.podd.restlet.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 * Test various forms of GetArtifact
 * 
 * @author kutila
 * 
 */
public class GetArtifactResourceImplTest extends AbstractResourceImplTest
{

    /**
     * Helper method to load an artifact
     * 
     * @return
     * @throws Exception
     */
    private InferredOWLOntologyID loadArtifact(String resourceName) throws Exception
    {
        InputStream in = this.getClass().getResourceAsStream(resourceName);
        InferredOWLOntologyID loadedArtifact = this.getApplication().getPoddArtifactManager().loadArtifact(in, RDFFormat.RDFXML);
        Assert.assertNotNull("Loaded artifact was null", loadedArtifact);
        return loadedArtifact;
    }
    
    /**
     * Test access without artifactID parameter gives a BAD_REQUEST error.
     */
    @Test
    public void testErrorGetArtifactWithoutArtifactId() throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        try
        {
            getArtifactClientResource.get(MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 400");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
    }
    
    /**
     * Test unauthenticated access gives an UNAUTHORIZED error.
     */
    @Test
    public void testErrorGetArtifactWithoutAuthentication() throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                "http://purl.org/podd/ns/artifact/artifact89");
        
        try
        {
            getArtifactClientResource.get(MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test authenticated access to get Artifact in HTML
     */
    @Test
    public void testGetArtifactBasicHtml() throws Exception
    {
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                "http://purl.org/podd/ns/artifact/artifact89");
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        Assert.assertTrue(body.contains("Administrator"));
    }
    
    /**
     * Test authenticated access to get Artifact in RDF/XML
     */
    @Test
    public void testGetArtifactBasicRdf() throws Exception
    {
        //prepare: add an artifact
        InferredOWLOntologyID loadedArtifact = loadArtifact("/test/artifacts/basicProject-1-internal-object.rdf");
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                loadedArtifact.getOntologyIRI().toString());
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();

        //verify: received contents are in RDF
        Assert.assertTrue("Result does not have RDF", body.contains("<rdf:RDF"));
        Assert.assertTrue("Result does not have RDF", body.endsWith("</rdf:RDF>"));

        //verify: received contents have artifact's ontology and version IRIs
        Assert.assertTrue("Result does not contain Ontology IRI", body.contains(loadedArtifact.getOntologyIRI().toString()));
        Assert.assertTrue("Result does not contain Version IRI", body.contains(loadedArtifact.getVersionIRI().toString()));
    }

    /**
     * Test authenticated access to get Artifact in RDF/Turtle
     */
    @Test
    public void testGetArtifactBasicTurtle() throws Exception
    {
        //prepare: add an artifact
        InferredOWLOntologyID loadedArtifact = loadArtifact("/test/artifacts/basicProject-1-internal-object.rdf");
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                loadedArtifact.getOntologyIRI().toString());
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();

        //verify: received contents are in Turtle
        Assert.assertTrue("Result does not have @prefix", body.contains("@prefix"));

        //verify: received contents have artifact's ontology and version IRIs
        Assert.assertTrue("Result does not contain Ontology IRI", body.contains(loadedArtifact.getOntologyIRI().toString()));
        Assert.assertTrue("Result does not contain Version IRI", body.contains(loadedArtifact.getVersionIRI().toString()));
    }

    /**
     * Test authenticated access to get Artifact in RDF/JSON
     */
    @Ignore
    @Test
    public void testGetArtifactBasicJson() throws Exception
    {
        //prepare: add an artifact
        InferredOWLOntologyID loadedArtifact = loadArtifact("/test/artifacts/basicProject-1-internal-object.rdf");
        
        final ClientResource getArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_GET_BASE));
        
        getArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER,
                loadedArtifact.getOntologyIRI().toString());
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(getArtifactClientResource, Method.GET, null,
                        RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        System.out.println(body);
        
        //verify: received contents are in RDF/JSON
//        Assert.assertTrue("Result does not have @prefix", body.contains("@prefix"));

        //verify: received contents have artifact's ontology and version IRIs
        Assert.assertTrue("Result does not contain Ontology IRI", body.contains(loadedArtifact.getOntologyIRI().toString()));
        Assert.assertTrue("Result does not contain Version IRI", body.contains(loadedArtifact.getVersionIRI().toString()));
    }

}
