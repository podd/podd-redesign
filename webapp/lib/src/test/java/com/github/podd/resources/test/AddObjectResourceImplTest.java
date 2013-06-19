/**
 * 
 */
package com.github.podd.resources.test;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class AddObjectResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test viewing the edit HTML page for creating a new Project.
     */
    @Test
    public void testGetAddTopObjectHtml() throws Exception
    {
        final ClientResource addObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_OBJECT_ADD));
        
        // mandatory parameter object-type
        addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER,
                "http://purl.org/podd/ns/poddScience#Project");
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(addObjectClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify:
        // System.out.println(body);
        Assert.assertTrue("Page title not as expected", body.contains("Add new Project"));
        Assert.assertTrue("object type not set in script",
                body.contains("podd.objectTypeUri = 'http://purl.org/podd/ns/poddScience#Project'"));
        
        this.assertFreemarker(body);
    }
    
    /**
     * Test viewing the edit HTML page for creating a new Publication object.
     */
    @Test
    public void testGetAddPublicationObjectHtml() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID ontologyID =
                this.loadTestArtifact("/test/artifacts/basic-2.rdf", MediaType.APPLICATION_RDF_XML);
        
        final ClientResource addObjectClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_OBJECT_ADD));
        
        // mandatory parameter object-type
        addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_TYPE_IDENTIFIER,
                "http://purl.org/podd/ns/poddScience#Publication");
        
        addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, ontologyID.getOntologyIRI()
                .toString());
        
        String parentUri = "http://purl.org/podd/ns/some-random-top-object-uri";
        addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_PARENT_IDENTIFIER, parentUri);
        
        addObjectClientResource.addQueryParameter(PoddWebConstants.KEY_PARENT_PREDICATE_IDENTIFIER,
                "http://purl.org/podd/ns/poddScience#hasPublication");
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(addObjectClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify:
        // System.out.println(body);
        Assert.assertTrue("Page title not as expected", body.contains("Add new Publication"));
        Assert.assertTrue("object type not set",
                body.contains("podd.objectTypeUri = 'http://purl.org/podd/ns/poddScience#Publication'"));
        Assert.assertTrue("parent URI not set",
                body.contains("podd.parentUri = 'http://purl.org/podd/ns/some-random-top-object-uri'"));
        Assert.assertTrue("parent predicate not set",
                body.contains("podd.parentPredicateUri = 'http://purl.org/podd/ns/poddScience#hasPublication'"));
        
        this.assertFreemarker(body);
    }
    
}
