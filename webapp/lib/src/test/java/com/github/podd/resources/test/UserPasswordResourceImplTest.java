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
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 *
 */
public class UserPasswordResourceImplTest extends AbstractResourceImplTest
{
    
    @Test
    public void testPasswordChangePageHtml() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final ClientResource userPasswordClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_PWD + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userPasswordClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        //System.out.println(body);
        this.assertFreemarker(body);
        
        Assert.assertTrue("Page missing User identifier", body.contains(testIdentifier));
        Assert.assertTrue("Page missing old password", body.contains("Old Password"));
        Assert.assertTrue("Page missing confirm password", body.contains("Confirm New Password"));
        Assert.assertTrue("Page missing save button", body.contains("Save Password"));
        Assert.assertTrue("Page missing cancel button", body.contains("Cancel"));
    }
     
}
