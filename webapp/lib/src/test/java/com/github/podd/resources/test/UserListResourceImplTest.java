/**
 * 
 */
package com.github.podd.resources.test;

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
import com.github.podd.utils.PODD;
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
        final ClientResource userListClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_LIST));
        try
        {
            this.doTestAuthenticatedRequest(userListClientResource, Method.GET, null,
                    MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, AbstractResourceImplTest.NO_ADMIN);
            Assert.fail("Should've failed due to lack of authorization");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(userListClientResource);
        }
    }
    
    @Test
    public void testGetUsersHtml() throws Exception
    {
        final ClientResource userListClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_LIST));
        
        try
        {
            final Representation results =
                    this.doTestAuthenticatedRequest(userListClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = this.getText(results);
            System.out.println(body);
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(userListClientResource);
        }
    }
    
    @Test
    public void testGetUsersRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userListClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_LIST));
        try
        {
            final Representation results =
                    this.doTestAuthenticatedRequest(userListClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final Model resultsModel = this.assertRdf(results, format, 16);
            
            // verify:
            final Set<Resource> subjects =
                    resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects();
            Assert.assertEquals("Not the expected number of Users", 2, subjects.size());
            
            Assert.assertEquals(
                    "Missing testAdminUser",
                    1,
                    resultsModel
                            .filter(null, SesameRealmConstants.OAS_USERIDENTIFIER,
                                    PODD.VF.createLiteral("testAdminUser")).subjects().size());
            
            Assert.assertEquals(
                    "Missing anotherUser",
                    1,
                    resultsModel
                            .filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, PODD.VF.createLiteral("anotherUser"))
                            .subjects().size());
        }
        finally
        {
            this.releaseClient(userListClientResource);
        }
    }
    
}
