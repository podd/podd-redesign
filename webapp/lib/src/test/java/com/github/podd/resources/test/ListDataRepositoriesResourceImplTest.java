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
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.test.RestletTestUtils;
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
}
