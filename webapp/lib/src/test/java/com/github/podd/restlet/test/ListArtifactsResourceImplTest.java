/**
 * 
 */
package com.github.podd.restlet.test;

import org.junit.Assert;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class ListArtifactsResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test authenticated access to list Artifacts in HTML
     */
    @Test
    public void testListArtifactsBasicHtml() throws Exception
    {
        // prepare: add two artifacts
        final String artifactUri1 = this.loadTestArtifact("/test/artifacts/basicProject-1-internal-object.rdf");
        final String artifactUri2 = this.loadTestArtifact("/test/artifacts/basic-2-internal-objects.rdf");
        
        final ClientResource listArtifactsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_LIST));
        
        listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_PUBLISHED, "true");
        listArtifactsClientResource.addQueryParameter(PoddWebConstants.KEY_UNPUBLISHED, "true");
        
        // Representation results = listArtifactsClientResource.get(MediaType.TEXT_HTML);
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(listArtifactsClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify:
        System.out.println("results:" + body);
        Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
        Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
        
        Assert.assertTrue("Missing heading on page - Artifacts Listing", body.contains("Artifacts Listing"));
        Assert.assertTrue("Missng artifact 1 URI on page", body.contains(artifactUri1));
        Assert.assertTrue("Missng artifact 2 URI on page", body.contains(artifactUri2));
        
        this.assertFreemarker(body);
    }
    
}
