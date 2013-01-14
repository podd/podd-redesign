/**
 * 
 */
package com.github.podd.restlet.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.html.FormData;
import org.restlet.ext.html.FormDataSet;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * An example use of Restlet for testing.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 *         NOTE: This class is ignored as it is only an example.
 */
@Ignore
public class ExampleResourceImplTest extends AbstractResourceImplTest
{
    
    /*
     * (non-Javadoc)
     * 
     * @see net.maenad.oas.webservice.impl.test.AbstractResourceImplTest#setUp()
     */
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see net.maenad.oas.webservice.impl.test.AbstractResourceImplTest#tearDown()
     */
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    /**
     * Tests that no error occurs when trying to post a valid form with authentication with all
     * fields filled.
     */
    @Test
    public void testCreateUserPutValidAuthenticatedFull() throws Exception
    {
        final ClientResource creationClientResource = new ClientResource(getUrl("/user/create"));
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(false);
        form.setMediaType(MediaType.APPLICATION_WWW_FORM);
        form.getEntries().add(new FormData("username", "testNewUser"));
        form.getEntries().add(new FormData("password", "testNewPassword"));
        form.getEntries().add(new FormData("email", "test@email.com"));
        form.getEntries().add(new FormData("lastName", "Usersmith"));
        form.getEntries().add(new FormData("firstName", "Mister"));
        
        final Representation results =
                PoddRestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.PUT, form,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
    }
    
    /**
     * Tests that no error occurs when trying to post a valid form with authentication with only
     * required fields filled.
     */
    @Test
    public void testCreateUserPutValidAuthenticatedMinimal() throws Exception
    {
        final ClientResource creationClientResource = new ClientResource(getUrl("/user/create"));
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(false);
        form.setMediaType(MediaType.APPLICATION_WWW_FORM);
        form.getEntries().add(new FormData("username", "testNewUser"));
        form.getEntries().add(new FormData("password", "testNewPassword"));
        form.getEntries().add(new FormData("email", "test@email.com"));
        
        final Representation results =
                PoddRestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.PUT, form,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
    }
    
    /**
     * Tests that an error occurs when trying to create a user without submitting a form
     */
    @Test
    public void testErrorCreateUserPutNoContent() throws Exception
    {
        try
        {
            final ClientResource creationClientResource = new ClientResource(getUrl("/user/create"));
            
            final Representation results =
                    PoddRestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.PUT, null,
                            MediaType.MULTIPART_FORM_DATA, Status.SUCCESS_CREATED, this.testWithAdminPrivileges);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(final ResourceException rex)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, rex.getStatus());
        }
    }
    
    /**
     * Tests that an error occurs when trying to get the create user resource while unauthenticated.
     */
    @Test
    public void testErrorCreateUserPutNotAuthenticated() throws Exception
    {
        try
        {
            final ClientResource creationClientResource = new ClientResource(getUrl("/user/create"));
            
            final FormDataSet form = new FormDataSet();
            form.setMultipart(true);
            form.getEntries().add(new FormData("username", "testNewUser"));
            form.getEntries().add(new FormData("password", "testNewPassword"));
            form.getEntries().add(new FormData("email", "test@email.com"));
            
            creationClientResource.put(form);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(final ResourceException rex)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, rex.getStatus());
        }
    }
    
    /**
     * Tests that an error occurs when trying to get the create user resource while authenticated
     * but without the admin role.
     */
    @Test
    public void testErrorCreateUserPutNotAuthorised() throws Exception
    {
        try
        {
            final ClientResource creationClientResource = new ClientResource(getUrl("/user/create"));
            
            final FormDataSet form = new FormDataSet();
            form.setMultipart(true);
            form.getEntries().add(new FormData("username", "testNewUser"));
            form.getEntries().add(new FormData("password", "testNewPassword"));
            form.getEntries().add(new FormData("email", "test@email.com"));
            
            final Representation results =
                    PoddRestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.PUT, null,
                            MediaType.TEXT_HTML, Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(final ResourceException rex)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, rex.getStatus());
        }
    }
    
    /**
     * Tests that no error occurs when trying to get the get user resource while authenticated with
     * the admin role.
     */
    @Test
    public void testGetUserBasicAuthorised() throws Exception
    {
        final ClientResource creationClientResource = new ClientResource(getUrl("/user/create"));
        
        final Representation results =
                PoddRestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
    }
    
    /**
     * Tests that no error occurs when trying to get the get user resource while authenticated with
     * the admin role.
     */
    @Test
    public void testGetUserBasicAuthorisedNonAdmin() throws Exception
    {
        final ClientResource creationClientResource = new ClientResource(getUrl("/user"));
        
        final Representation results =
                PoddRestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testNoAdminPrivileges);
        
    }
    
}
