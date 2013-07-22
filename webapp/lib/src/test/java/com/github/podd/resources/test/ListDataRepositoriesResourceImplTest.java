/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.RestletUtilMediaType;
import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class ListDataRepositoriesResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testHtmlAuthenticatedAdmin() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        Representation result =
                RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final String body = result.getText();
        
        // System.out.println(body);
        
        // verify:
        assertFreemarker(body);
        
        Assert.assertFalse(body.contains("No data repositories currently available"));
        
        Assert.assertTrue(body.contains("alias_local_ssh"));
    }
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testHtmlAuthenticatedNonAdmin() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                    MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
            Assert.fail("Did not find expected exception");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testHtmlUnauthenticated() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        try
        {
            dataRepositoriesClientResource.get(MediaType.TEXT_HTML);
            Assert.fail("Did not find expected exception");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testRdfXmlAuthenticatedAdmin() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        Representation result =
                RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                        MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = result.getText();
        
        // verify:
        final Model model = this.assertRdf(new StringReader(body), RDFFormat.RDFXML, 3);
        
        // DebugUtils.printContents(model);
    }
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testRdfXmlAuthenticatedNonAdmin() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                    MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
            Assert.fail("Did not find expected exception");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testRdfXmlUnauthenticated() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        try
        {
            dataRepositoriesClientResource.get(MediaType.APPLICATION_RDF_XML);
            Assert.fail("Did not find expected exception");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testRdfJsonAuthenticatedAdmin() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        Representation result =
                RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                        RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = result.getText();
        
        // verify:
        final Model model = this.assertRdf(new StringReader(body), RDFFormat.RDFJSON, 3);
        
        // DebugUtils.printContents(model);
    }
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testRdfJsonAuthenticatedNonAdmin() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(dataRepositoriesClientResource, Method.GET, null,
                    RestletUtilMediaType.APPLICATION_RDF_JSON, Status.SUCCESS_OK, this.testNoAdminPrivileges);
            Assert.fail("Did not find expected exception");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    /**
     * Test requesting data repositories as administrator.
     */
    @Test
    public void testRdfJsonUnauthenticated() throws Exception
    {
        final ClientResource dataRepositoriesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_DATA_REPOSITORY_LIST));
        
        try
        {
            dataRepositoriesClientResource.get(RestletUtilMediaType.APPLICATION_RDF_JSON);
            Assert.fail("Did not find expected exception");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Not the expected HTTP status code", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
}
