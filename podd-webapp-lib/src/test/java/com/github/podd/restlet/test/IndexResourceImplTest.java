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

import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class IndexResourceImplTest extends AbstractResourceImplTest
{
    /**
     * Test unauthenticated access to /index
     */
    @Test
    public void testGetIndexWithoutAuthentication() throws Exception
    {
        final ClientResource indexClientResource = new ClientResource(this.getUrl("/" + PoddWebConstants.PATH_INDEX));
        
        final Representation results =
                PoddRestletTestUtils.doTestUnAuthenticatedRequest(indexClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK);
        
        final String body = results.getText();
        Assert.assertTrue(body.contains("Welcome to PODD, please"));
    }
    
}
