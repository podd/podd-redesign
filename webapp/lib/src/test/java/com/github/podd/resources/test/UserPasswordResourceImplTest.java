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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
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
public class UserPasswordResourceImplTest extends AbstractResourceImplTest
{
    
    @Test
    public void testChangeOtherUserPasswordRdf() throws Exception
    {
        final String testIdentifier = "anotherUser";
        final String newPassword = "modifiedPassword";
        final URI tempUserUri = PODD.VF.createURI("urn:temp:user");
        
        // prepare: create Model with modified password and user identifier
        final Model userInfoModel = new LinkedHashModel();
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER, PODD.VF.createLiteral(testIdentifier));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET, PODD.VF.createLiteral(newPassword));
        
        // submit new password to Change Password Service
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userPasswordClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_PWD));
        try
        {
            userPasswordClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rio.write(userInfoModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            
            final Representation modifiedResults =
                    this.doTestAuthenticatedRequest(userPasswordClientResource, Method.POST, input, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            // verify: response has correct identifier
            final Model model = this.assertRdf(new StringReader(this.getText(modifiedResults)), RDFFormat.RDFXML, 1);
            Assert.assertEquals("Unexpected user identifier", testIdentifier,
                    model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        }
        finally
        {
            this.releaseClient(userPasswordClientResource);
        }
        
        // verify: request with new login details should succeed
        final ClientResource userDetailsClientResource2 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        userDetailsClientResource2.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
        try
        {
            this.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType, Status.SUCCESS_OK,
                    testIdentifier, newPassword.toCharArray());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource2);
        }
        
        // verify: request with old login details should fail
        final ClientResource userDetailsClientResource3 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        userDetailsClientResource3.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
        try
        {
            this.doTestAuthenticatedRequest(userDetailsClientResource3, Method.GET, null, mediaType,
                    Status.CLIENT_ERROR_UNAUTHORIZED, AbstractResourceImplTest.NO_ADMIN);
            Assert.fail("Should have thrown a ResourceException as password should now be invalid");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Was expecting an UNAUTHORIZED error", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource3);
        }
    }
    
    @Test
    public void testChangeOwnPasswordRdf() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final String oldPassword = "testAdminPassword";
        final String newPassword = "modifiedPassword";
        final URI tempUserUri = PODD.VF.createURI("urn:temp:user");
        
        // prepare: create Model with modified password and user identifier
        final Model userInfoModel = new LinkedHashModel();
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER, PODD.VF.createLiteral(testIdentifier));
        userInfoModel.add(tempUserUri, PODD.PODD_USER_OLDSECRET, PODD.VF.createLiteral(oldPassword));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET, PODD.VF.createLiteral(newPassword));
        
        // submit new password to Change Password Service
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userPasswordClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_PWD));
        try
        {
            userPasswordClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rio.write(userInfoModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            
            final Representation modifiedResults =
                    this.doTestAuthenticatedRequest(userPasswordClientResource, Method.POST, input, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            // verify: response has correct identifier
            final Model model = this.assertRdf(modifiedResults, RDFFormat.RDFXML, 1);
            Assert.assertEquals("Unexpected user identifier", testIdentifier,
                    model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        }
        finally
        {
            this.releaseClient(userPasswordClientResource);
        }
        
        // verify: request with new login details should succeed
        final ClientResource userDetailsClientResource2 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        
        try
        {
            userDetailsClientResource2.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            this.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType, Status.SUCCESS_OK,
                    RestletTestUtils.TEST_ADMIN_USERNAME, newPassword.toCharArray());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource2);
        }
        
        // verify: request with old login details should fail
        final ClientResource userDetailsClientResource3 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        
        try
        {
            userDetailsClientResource3.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            this.doTestAuthenticatedRequest(userDetailsClientResource3, Method.GET, null, mediaType, Status.SUCCESS_OK,
                    RestletTestUtils.TEST_ADMIN_USERNAME, oldPassword.toCharArray());
            Assert.fail("Should have thrown a ResourceException as password should now be invalid");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Was expecting an UNAUTHORIZED error", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource3);
        }
    }
    
    @Test
    public void testPasswordChangePageHtml() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final ClientResource userPasswordClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_PWD));
        try
        {
            userPasswordClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation results =
                    this.doTestAuthenticatedRequest(userPasswordClientResource, Method.GET, null, MediaType.TEXT_HTML,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = this.getText(results);
            // System.out.println(body);
            this.assertFreemarker(body);
            
            Assert.assertTrue("Page missing User identifier", body.contains(testIdentifier));
            Assert.assertTrue("Page missing old password", body.contains("Old Password"));
            Assert.assertTrue("Page missing confirm password", body.contains("Confirm New Password"));
            Assert.assertTrue("Page missing save button", body.contains("Save Password"));
            Assert.assertTrue("Page missing cancel button", body.contains("Cancel"));
        }
        finally
        {
            this.releaseClient(userPasswordClientResource);
        }
    }
    
}
