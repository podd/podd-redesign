/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.resources.test;

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

import com.github.ansell.restletutils.test.RestletTestUtils;

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
        final ClientResource creationClientResource = new ClientResource(this.getUrl("/user/create"));
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(false);
        form.setMediaType(MediaType.APPLICATION_WWW_FORM);
        form.getEntries().add(new FormData("username", "testNewUser"));
        form.getEntries().add(new FormData("password", "testNewPassword"));
        form.getEntries().add(new FormData("email", "test@email.com"));
        form.getEntries().add(new FormData("lastName", "Usersmith"));
        form.getEntries().add(new FormData("firstName", "Mister"));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.PUT, form,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.WITH_ADMIN);
        
    }
    
    /**
     * Tests that no error occurs when trying to post a valid form with authentication with only
     * required fields filled.
     */
    @Test
    public void testCreateUserPutValidAuthenticatedMinimal() throws Exception
    {
        final ClientResource creationClientResource = new ClientResource(this.getUrl("/user/create"));
        
        final FormDataSet form = new FormDataSet();
        form.setMultipart(false);
        form.setMediaType(MediaType.APPLICATION_WWW_FORM);
        form.getEntries().add(new FormData("username", "testNewUser"));
        form.getEntries().add(new FormData("password", "testNewPassword"));
        form.getEntries().add(new FormData("email", "test@email.com"));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.PUT, form,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.WITH_ADMIN);
        
    }
    
    /**
     * Tests that an error occurs when trying to create a user without submitting a form
     */
    @Test
    public void testErrorCreateUserPutNoContent() throws Exception
    {
        try
        {
            final ClientResource creationClientResource = new ClientResource(this.getUrl("/user/create"));
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.PUT, null,
                            MediaType.MULTIPART_FORM_DATA, Status.SUCCESS_CREATED, this.WITH_ADMIN);
            
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
            final ClientResource creationClientResource = new ClientResource(this.getUrl("/user/create"));
            
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
            final ClientResource creationClientResource = new ClientResource(this.getUrl("/user/create"));
            
            final FormDataSet form = new FormDataSet();
            form.setMultipart(true);
            form.getEntries().add(new FormData("username", "testNewUser"));
            form.getEntries().add(new FormData("password", "testNewPassword"));
            form.getEntries().add(new FormData("email", "test@email.com"));
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.PUT, null,
                            MediaType.TEXT_HTML, Status.CLIENT_ERROR_UNAUTHORIZED, this.NO_ADMIN);
            
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
        final ClientResource creationClientResource = new ClientResource(this.getUrl("/user/create"));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.WITH_ADMIN);
        
    }
    
    /**
     * Tests that no error occurs when trying to get the get user resource while authenticated with
     * the admin role.
     */
    @Test
    public void testGetUserBasicAuthorisedNonAdmin() throws Exception
    {
        final ClientResource creationClientResource = new ClientResource(this.getUrl("/user"));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(creationClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.NO_ADMIN);
        
    }
    
}
