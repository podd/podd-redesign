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
import java.io.StringWriter;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
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
import com.github.podd.utils.PoddRoles;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * Test User Edit Resource at "user/edit/{identifier}"
 * 
 * @author kutila
 * 
 */
public class UserEditResourceImplTest extends AbstractResourceImplTest
{
    
    /**
     * Test display of current user Edit page
     */
    @Test
    public void testEditCurrentUserHtml() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final ClientResource userEditClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT));
        try
        {
            userEditClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = this.getText(results);
            System.out.println(body);
            this.assertFreemarker(body);
            
            Assert.assertTrue("Page missing User identifier", body.contains(testIdentifier));
            Assert.assertTrue("Page missing first name", body.contains("Initial Admin"));
            Assert.assertTrue("Page missing last name", body.contains("User"));
            Assert.assertTrue("Page missing organization", body.contains("Local Organisation"));
            Assert.assertTrue("Page missing home page", body.contains("http://www.example.com/testAdmin"));
            Assert.assertTrue("Page missing orcid", body.contains("Dummy-ORCID"));
        }
        finally
        {
            this.releaseClient(userEditClientResource);
        }
    }
    
    /**
     * Test authenticated edit of current user details
     */
    @Test
    public void testEditCurrentUserRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final String testIdentifier = "testAdminUser";
        
        // prepare: retrieve Details of existing User
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
        
        try
        {
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final Model userInfoModel = this.assertRdf(results, format, 11);
            // this.log.info("Retrieved [{}] details. ", testIdentifier);
            // DebugUtils.printContents(userInfoModel);
            
            // prepare: modify existing User's details
            final String modifiedFirstName = "Totally";
            final String modifiedLastName = "Newman";
            
            final Resource userUri =
                    userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
            
            userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
            userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
            userInfoModel
                    .add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, PODD.VF.createLiteral(modifiedFirstName));
            userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME, PODD.VF.createLiteral(modifiedLastName));
            // submit modified details to Edit User Service
            final ClientResource userEditClientResource =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT));
            try
            {
                userEditClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                Rio.write(userInfoModel, out, format);
                final Representation input = new StringRepresentation(out.toString(), mediaType);
                
                final Representation modifiedResults =
                        RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input,
                                mediaType, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
                
                // verify: response has correct identifier
                final Model model = this.assertRdf(modifiedResults, RDFFormat.RDFXML, 1);
                Assert.assertEquals("Unexpected user identifier", testIdentifier,
                        model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
                
                // verify: details have been correctly updated (by retrieving
                // User details again)
                final ClientResource userDetailsClientResource2 =
                        new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
                try
                {
                    userDetailsClientResource2.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                    
                    final Representation updatedResults =
                            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null,
                                    mediaType, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
                    
                    final Model resultsModel = this.assertRdf(updatedResults, format, 11);
                    
                    Assert.assertEquals("Unexpected user identifier", testIdentifier,
                            resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
                    Assert.assertEquals("Unexpected user URI", userUri.stringValue(),
                            resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects()
                                    .iterator().next().stringValue());
                    Assert.assertEquals("First name was not modified", modifiedFirstName,
                            resultsModel.filter(null, SesameRealmConstants.OAS_USERFIRSTNAME, null).objectString());
                    Assert.assertEquals("Last name was not modified", modifiedLastName,
                            resultsModel.filter(null, SesameRealmConstants.OAS_USERLASTNAME, null).objectString());
                    Assert.assertEquals("Role count should not have changed", 1,
                            resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects().size());
                }
                finally
                {
                    this.releaseClient(userDetailsClientResource2);
                }
            }
            finally
            {
                this.releaseClient(userEditClientResource);
            }
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
        
    }
    
    /**
     * Test display of another user's Edit page as Administrator
     */
    @Test
    public void testEditOtherUserHtml() throws Exception
    {
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final String testHomePage = "http:///www.john.doe.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PODD.VF
                .createURI("urn:podd:some-project")));
        final String testFirstName = "John";
        final String testLastName = "Doe";
        final String testOrganization = "CSIRO";
        final String testOrcid = "john-orcid";
        final String testTitle = "Mr";
        final String testPhone = "000333434";
        final String testAddress = "Some Address";
        final String testPosition = "Researcher";
        this.loadTestUser(testIdentifier, "testuserpassword", testFirstName, testLastName, testIdentifier,
                testHomePage, testOrganization, testOrcid, testTitle, testPhone, testAddress, testPosition, roles,
                PoddUserStatus.ACTIVE);
        
        final ClientResource userEditClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT));
        try
        {
            userEditClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = this.getText(results);
            // System.out.println(body);
            this.assertFreemarker(body);
            
            Assert.assertTrue("Page missing User identifier", body.contains(testIdentifier));
            Assert.assertTrue("Page missing title", body.contains(testTitle));
            Assert.assertTrue("Page missing first name", body.contains(testFirstName));
            Assert.assertTrue("Page missing last name", body.contains(testLastName));
            Assert.assertTrue("Page missing organization", body.contains(testOrganization));
            Assert.assertTrue("Page missing phone", body.contains(testPhone));
            Assert.assertTrue("Page missing position", body.contains(testPosition));
            Assert.assertTrue("Page missing address", body.contains(testAddress));
            Assert.assertTrue("Page missing home page", body.contains(testHomePage));
            Assert.assertTrue("Page missing orcid", body.contains(testOrcid));
        }
        finally
        {
            this.releaseClient(userEditClientResource);
        }
    }
    
    /**
     * Test authenticated edit of other user details as Administrator
     */
    @Test
    public void testEditOtherUserRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PODD.VF
                .createURI("urn:podd:some-project")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        // prepare: retrieve Details of existing User
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final Model userInfoModel = this.assertRdf(results, format, 16);
            // this.log.info("Retrieved [{}] details. ", testIdentifier);
            // DebugUtils.printContents(userInfoModel);
            
            // prepare: modify existing User's details
            final String modifiedFirstName = "Totally";
            final String modifiedLastName = "Newman";
            
            final Resource userUri =
                    userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
            
            userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
            userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
            userInfoModel.remove(userUri, PODD.PODD_USER_STATUS, null);
            userInfoModel
                    .add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, PODD.VF.createLiteral(modifiedFirstName));
            userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME, PODD.VF.createLiteral(modifiedLastName));
            userInfoModel.add(userUri, PODD.PODD_USER_STATUS, PoddUserStatus.INACTIVE.getURI());
            
            // submit modified details to Edit User Service
            final ClientResource userEditClientResource =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT));
            try
            {
                userEditClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                Rio.write(userInfoModel, out, format);
                final Representation input = new StringRepresentation(out.toString(), mediaType);
                
                final Representation modifiedResults =
                        RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input,
                                mediaType, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
                
                // verify: response has correct identifier
                final Model model = this.assertRdf(modifiedResults, RDFFormat.RDFXML, 1);
                Assert.assertEquals("Unexpected user identifier", testIdentifier,
                        model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
                
                // verify: details have been correctly updated (by retrieving
                // User details again)
                final ClientResource userDetailsClientResource2 =
                        new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
                try
                {
                    userDetailsClientResource2.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                    
                    final Representation updatedResults =
                            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null,
                                    mediaType, Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
                    
                    final Model resultsModel = this.assertRdf(updatedResults, format, 16);
                    
                    Assert.assertEquals("Unexpected user identifier", testIdentifier,
                            resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
                    Assert.assertEquals("Unexpected user URI", userUri.stringValue(),
                            resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects()
                                    .iterator().next().stringValue());
                    Assert.assertEquals("First name was not modified", modifiedFirstName,
                            resultsModel.filter(null, SesameRealmConstants.OAS_USERFIRSTNAME, null).objectString());
                    Assert.assertEquals("Last name was not modified", modifiedLastName,
                            resultsModel.filter(null, SesameRealmConstants.OAS_USERLASTNAME, null).objectString());
                    Assert.assertEquals("Role count should not have changed", 1,
                            resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects().size());
                    Assert.assertEquals("Status was not modified", PoddUserStatus.INACTIVE.getURI(), resultsModel
                            .filter(null, PODD.PODD_USER_STATUS, null).objectURI());
                }
                finally
                {
                    this.releaseClient(userDetailsClientResource2);
                }
            }
            finally
            {
                this.releaseClient(userEditClientResource);
            }
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
    /**
     * Verify that changing password using the Edit User Interface has no effect
     */
    @Test
    public void testErrorEditCurrentUserPasswordRdf() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final String testPassword = "modifiedPassword";
        final URI tempUserUri = PODD.VF.createURI("urn:temp:user");
        
        // prepare: create Model with modified password and user identifier
        final Model userInfoModel = new LinkedHashModel();
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER, PODD.VF.createLiteral(testIdentifier));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET, PODD.VF.createLiteral(testPassword));
        userInfoModel.add(tempUserUri, PODD.PODD_USER_STATUS, PoddUserStatus.ACTIVE.getURI());
        
        // submit new password to Edit User Service
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userEditClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT));
        
        try
        {
            userEditClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rio.write(userInfoModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            
            final Representation modifiedResults =
                    RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            // verify: response has correct identifier
            final Model model = this.assertRdf(modifiedResults, RDFFormat.RDFXML, 1);
            Assert.assertEquals("Unexpected user identifier", testIdentifier,
                    model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
            
            // verify: request with old login details should still succeed
            final ClientResource userDetailsClientResource2 =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
            try
            {
                userDetailsClientResource2.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
                
            }
            catch(final ResourceException e)
            {
                Assert.fail("Should have succeeded as password was not changed");
            }
            finally
            {
                this.releaseClient(userDetailsClientResource2);
            }
        }
        finally
        {
            this.releaseClient(userEditClientResource);
        }
    }
    
    /**
     * Test error trying to edit a User that does not exist in the system
     */
    @Test
    public void testErrorEditNonExistentUserRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        
        final Representation input = new StringRepresentation("Should have user model in JSON", mediaType);
        
        // submit modified details to Edit User Service
        final ClientResource userEditClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT));
        
        try
        {
            userEditClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, "noSuchUser");
            
            RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                    Status.CLIENT_ERROR_BAD_REQUEST, AbstractResourceImplTest.WITH_ADMIN);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(e.getStatus(), Status.CLIENT_ERROR_BAD_REQUEST);
        }
        finally
        {
            this.releaseClient(userEditClientResource);
        }
    }
    
    /**
     * Test error trying to display other user Edit page as non-admin user
     */
    @Test
    public void testErrorEditOtherUserNonAdminHtml() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final ClientResource userEditClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT));
        
        try
        {
            userEditClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.GET, null, MediaType.TEXT_HTML,
                    Status.CLIENT_ERROR_UNAUTHORIZED, AbstractResourceImplTest.NO_ADMIN);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals("Expected UNAUTHORIZED error", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(userEditClientResource);
        }
    }
    
    /**
     * Test error trying to edit other user details as non-admin user
     */
    @Test
    public void testErrorEditOtherUserNonAdminRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PODD.VF
                .createURI("urn:podd:some-project")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        // prepare: retrieve Details of existing User
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final Model userInfoModel = this.assertRdf(results, format, 16);
            // this.log.info("Retrieved [{}] details. ", testIdentifier);
            // DebugUtils.printContents(userInfoModel);
            
            // prepare: modify existing User's details
            final String modifiedFirstName = "Totally";
            final String modifiedLastName = "Newman";
            
            final Resource userUri =
                    userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
            
            userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
            userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
            userInfoModel
                    .add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, PODD.VF.createLiteral(modifiedFirstName));
            userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME, PODD.VF.createLiteral(modifiedLastName));
            
            final StringWriter out = new StringWriter();
            Rio.write(userInfoModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            
            // try to submit modified details to Edit User Service
            final ClientResource userEditClientResource =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT));
            try
            {
                userEditClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                
                RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                        Status.CLIENT_ERROR_UNAUTHORIZED, AbstractResourceImplTest.NO_ADMIN);
                Assert.fail("Should have thrown a ResourceException due to lack of authorization");
            }
            catch(final ResourceException e)
            {
                Assert.assertEquals("Should have been Unauthorized", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
            }
            finally
            {
                this.releaseClient(userEditClientResource);
            }
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
}
