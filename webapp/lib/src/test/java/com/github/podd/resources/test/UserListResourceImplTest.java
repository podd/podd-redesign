/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.SesameRealmConstants;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class UserListResourceImplTest extends AbstractResourceImplTest
{
    
    @Test
    public void testErrorGetUsersNonAdminRdf() throws Exception
    {
        final ClientResource userListlientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_LIST));
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userListlientResource, Method.GET, null,
                    MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
            Assert.fail("Should've failed due to lack of authorization");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    @Test
    public void testGetUsersHtml() throws Exception
    {
        final ClientResource userListRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_LIST));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userListRolesClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        System.out.println(body);
        this.assertFreemarker(body);
        
    }
    
    @Test
    public void testGetUsersRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userListlientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_LIST));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userListlientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 16);
        
        // verify:
        final Set<Resource> subjects =
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects();
        Assert.assertEquals("Not the expected number of Users", 2, subjects.size());
        
        Assert.assertEquals(
                "Missing testAdminUser",
                1,
                resultsModel
                        .filter(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                                PoddRdfConstants.VF.createLiteral("testAdminUser")).subjects().size());
        
        Assert.assertEquals(
                "Missing anotherUser",
                1,
                resultsModel
                        .filter(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                                PoddRdfConstants.VF.createLiteral("anotherUser")).subjects().size());
    }
    
}
