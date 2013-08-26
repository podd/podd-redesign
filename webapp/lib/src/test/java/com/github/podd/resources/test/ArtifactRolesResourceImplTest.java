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
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 *
 */
public class ArtifactRolesResourceImplTest extends AbstractResourceImplTest
{
    
    @Test
    public void testGetArtifactRolesBasicHtml() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact(TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT);
        
        final ClientResource getArtifactRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_ROLES));
        
        getArtifactRolesClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(getArtifactRolesClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify:
        System.out.println(body);
        Assert.assertTrue("Page does not identify Administrator", body.contains("Administrator"));
        Assert.assertFalse("Page contained a 404 error", body.contains("ERROR: 404"));
        
        this.assertFreemarker(body);
        
    }
    
}
