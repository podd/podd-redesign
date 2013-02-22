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
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * @author kutila
 */
public class EditArtifactResourceImplTest extends AbstractResourceImplTest
{

    @Ignore
    @Test
    public void testEditArtifactBasicJson() throws Exception
    {
        Assert.fail("TODO: implement");
    }
    
    @Ignore
    @Test
    public void testEditArtifactBasicRdf() throws Exception
    {
        Assert.fail("TODO: implement");
    }
    
    @Ignore
    @Test
    public void testEditArtifactBasicTurtle() throws Exception
    {
        Assert.fail("TODO: implement");
    }

    /**
     * Test viewing the edit HTML page for an internal PODD object. 
     */
    @Test
    public void testGetEditArtifactInternalObjectHtml() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact("/test/artifacts/basic-1.rdf");
        
        final String objectUri = "urn:poddinternal:7616392e-802b-4c5d-953d-bf81da5a98f4:0";
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT_MERGE));
        
        editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectUri);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify:
        System.out.println(body);
        this.assertFreemarker(body);
    }
    
    /**
     * Test viewing the edit HTML page for a PODD top object (i.e. a Project). 
     */
    @Ignore
    @Test
    public void testGetEditArtifactTopObjectHtml() throws Exception
    {
        Assert.fail("TODO: implement");
    }

    /**
     * Test posting to the edit HTML page modifying an internal PODD object.
     */
    @Ignore
    @Test
    public void testPostEditArtifactInternalObjectHtml() throws Exception
    {
        Assert.fail("TODO: implement");
    }
    
    /**
     * Test posting to the edit HTML page modifying a PODD top object (i.e. a Project).
     */
    @Ignore
    @Test
    public void testPostEditArtifactTopObjectHtml() throws Exception
    {
        Assert.fail("TODO: implement");
    }
    
}
