/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.restlet.PoddRoles;
import com.github.podd.utils.PoddWebConstants;

/**
 * Test UserDetails resource at "user/{identifier}"
 * 
 * @author kutila
 * 
 */
public class UserDetailsResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test requesting details of a non-existent user results in a 404 response
     */
    @Test
    public void testErrorGetUserDetailsOfNonExistentUser() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + "noSuchUser"));
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                    MediaType.TEXT_HTML, Status.CLIENT_ERROR_NOT_FOUND, this.testWithAdminPrivileges);
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_NOT_FOUND, e.getStatus());
        }
    }
    
    /**
     * Test authenticated user requesting details of another user is not allowed
     */
    @Test
    public void testErrorGetUserDetailsOfOtherUserByNonAdmin() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + "testAdminUser"));
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                    MediaType.TEXT_HTML, Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException with Status Code 401");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test unauthenticated access gives an UNAUTHORIZED error.
     */
    @Test
    public void testErrorGetUserDetailsWithoutAuthentication() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + "testAdminUser"));
        
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
     * Test authenticated Admin user requesting details of another user
     */
    @Test
    public void testGetUserDetailsOfOtherUserByAdministrator() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + "testUser"));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        Assert.assertTrue(body.contains("User Name: "));
        Assert.assertTrue(body.contains("test.user"));
        this.assertFreemarker(body);
    }
    
    /**
     * Test authenticated access to user details of current user
     */
    @Test
    public void testGetUserDetailsWithAuthentication() throws Exception
    {
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + "testAdminUser"));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        Assert.assertTrue(body.contains("Personal Details"));
        Assert.assertTrue(body.contains("User Name: "));
        Assert.assertTrue(body.contains("test.admin.user@example.com"));
        this.assertFreemarker(body);
    }
    
    /**
     * Test authenticated access to user details of current user
     */
    @Test
    public void testGetUserRdfBasic() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + "testAdminUser"));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 16);
        
        // DebugUtils.printContents(resultsModel);
        Assert.assertEquals("Not the expected identifier", "testAdminUser",
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        
        // verify: Roles are valid PoddRoles
        final Set<Value> roleSet = resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects();
        Assert.assertEquals("Not expected number of Roles", 3, roleSet.size());
        final Iterator<Value> iterator = roleSet.iterator();
        while(iterator.hasNext())
        {
            final Value next = iterator.next();
            final RestletUtilRole roleByUri = PoddRoles.getRoleByUri((URI)next);
            Assert.assertNotNull("Role is not a PoddRole", roleByUri);
        }
    }
    
}
