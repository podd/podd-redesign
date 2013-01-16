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
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.PoddWebConstants;

/**
 * Test UserDetails resource at "to /userdetails"
 * 
 * @author kutila
 * 
 */
public class UserDetailsResourceImplTest extends AbstractResourceImplTest
{
    /**
     * Test unauthenticated access gives an UNAUTHORIZED error.
     */
    @Test
    public void testErrorGetUserDetailsWithoutAuthentication() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl("/" + PoddWebConstants.PATH_USER_DETAILS));
        
        try
        {
            userDetailsClientResource.get(MediaType.TEXT_HTML);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test authenticated access
     */
    @Test
    public void testGetUserDetailsWithAuthentication() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl("/" + PoddWebConstants.PATH_USER_DETAILS));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        Assert.assertTrue(body.contains("User Name: "));
        Assert.assertTrue(body.contains("john.test"));
    }
    
}
