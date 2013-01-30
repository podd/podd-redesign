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
 * Test View Artifacts resource at "artifact/view"
 * 
 * @author kutila
 *
 */
public class ViewArtifactResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test authenticated access to user details of current user
     */
    @Test
    public void testViewArtifactSimple() throws Exception
    {
        final ClientResource viewArtifactClientResource =
                new ClientResource(this.getUrl("/" + PoddWebConstants.PATH_ARTIFACT_VIEW));
        
        viewArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, "http://purl.org/podd/ns/artifact/artifact89");
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(viewArtifactClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        Assert.assertTrue(body.contains("Administrator"));
        
        this.assertFreemarker(body);
    }
    
}
