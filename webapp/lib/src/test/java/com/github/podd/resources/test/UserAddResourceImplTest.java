/*
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.vocabulary.RDF;
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
import com.github.podd.restlet.PoddRoles;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class UserAddResourceImplTest extends AbstractResourceImplTest
{
 
    /**
     * Test display of add new user page
     */
    @Test
    public void testAddUserHtml() throws Exception
    {
        final ClientResource userAddClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        this.assertFreemarker(body);
        
        System.out.println(body);
        
        Assert.assertTrue("Page missing INACTIVE status", body.contains(PoddUserStatus.INACTIVE.getLabel()));
        Assert.assertTrue("Page missing password field", body.contains("password"));
    }
    
    /**
     * Test adding a PoddUser without using the utility method AbstractResourceImplTest.loadTestUser() 
     */
    @Test
    public void testAddUserBasicRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: create a Model of user
        final String testEmail = "testuser@restlet-test.org";
        final String testPassword = "testpassword";
        final String testFirstName = "First";
        final String testLastName = "Last";
        
        final Model userInfoModel = new LinkedHashModel();
        final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                PoddRdfConstants.VF.createLiteral(testEmail));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                PoddRdfConstants.VF.createLiteral(testPassword));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral(testFirstName));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral(testLastName));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_HOMEPAGE,
                PoddRdfConstants.VF.createURI("http://nohomepage"));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORGANIZATION,
                PoddRdfConstants.VF.createLiteral("n/a"));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORCID, PoddRdfConstants.VF.createLiteral("n/a"));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_STATUS, PoddUserStatus.INACTIVE.getURI());
        
        userInfoModel
                .add(tempUserUri, SesameRealmConstants.OAS_USEREMAIL, PoddRdfConstants.VF.createLiteral(testEmail));
        
        // prepare: add 'Repository Admin User' Role
        final URI authenticatedRoleMapping =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
        userInfoModel.add(authenticatedRoleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
        userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                PoddRoles.ADMIN.getURI());
        
        // prepare: add 'Project Observer' Role of an imaginary project
        final URI observerRoleMapping =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
        userInfoModel.add(observerRoleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        userInfoModel.add(observerRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
        userInfoModel.add(observerRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                PoddRoles.PROJECT_OBSERVER.getURI());
        userInfoModel.add(observerRoleMapping, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT,
                PoddRdfConstants.VF.createURI("urn:podd:some:project"));
        
        final ClientResource userAddClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: response has same correct identifier
        final Model model =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testEmail,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
    }
    
    @Test
    public void testAddUserWithAllAttributesRdf() throws Exception
    {
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project")));
        String testUserUri =
                this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                        "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address",
                        "Researcher", roles, PoddUserStatus.ACTIVE);

        // verify: 
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);

        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 19);
        
        com.github.podd.utils.DebugUtils.printContents(resultsModel);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Unexpected user URI", testUserUri,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next().stringValue());
        Assert.assertEquals("Unexpected user Status", PoddUserStatus.ACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
    }
    
    /**
     * Tests that a new User created without any Role Mappings is given the Project_creator Role 
     */
    @Test
    public void testAddUserWithNoRolesMapsProjectCreatorRoleRdf() throws Exception
    {
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        String testUserUri = this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier, null, null,
                null, null, null, null, null, roles, null);

        // verify: 
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);

        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 8);
        
        com.github.podd.utils.DebugUtils.printContents(resultsModel);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Unexpected user URI", testUserUri,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next().stringValue());
        Assert.assertEquals("User Status was not set to INACTIVE by default", PoddUserStatus.INACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
        
        // verify: Project Creator Role has been assigned by default
        final Set<Resource> roleMappings = resultsModel.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING).subjects();
        Assert.assertEquals("No Role Mappings set", 1, roleMappings.size());
        final Resource roleMappingUri = (Resource) roleMappings.toArray()[0];
        Assert.assertEquals("Project_Creator Role not mapped", PoddRoles.PROJECT_CREATOR.getURI(),
                resultsModel.filter(roleMappingUri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objectURI());
    }

    @Test
    public void testAddUserWithOnlyMandatoryAttributesRdf() throws Exception
    {
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project")));
        String testUserUri = this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier, null, null,
                null, null, null, null, null, roles, null);

        // verify: 
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);

        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 12);
        
        com.github.podd.utils.DebugUtils.printContents(resultsModel);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Unexpected user URI", testUserUri,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next().stringValue());
        Assert.assertEquals("User Status was not set to INACTIVE by default", PoddUserStatus.INACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
    }

    /**
     * Test displaying of add new user page fails when not an administrative user
     */
    @Test
    public void testErrorAddUserWithoutAdminPrivilegesHtml() throws Exception
    {
        final ClientResource userAddClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
        
        try
        {
                RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
                Assert.fail("Should have thrown a ResourceException");
        }
        catch (ResourceException e)
        {
            Assert.assertEquals("Expected an UNAUTHORIZED error", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
    
    @Test
    public void testErrorAddUserWithoutEmailRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: create a Model of user
        final String testIdentifier = "wrong@restlet-test.org";
        final String testPassword = "testpassword";
        final String testFirstName = "First";
        final String testLastName = "Last";
        
        final Model userInfoModel = new LinkedHashModel();
        final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                PoddRdfConstants.VF.createLiteral(testIdentifier));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                PoddRdfConstants.VF.createLiteral(testPassword));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral(testFirstName));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral(testLastName));
        
        // prepare: add 'Authenticated User' Role
        final URI authenticatedRoleMapping =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping:", UUID.randomUUID().toString());
        userInfoModel.add(authenticatedRoleMapping, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
        userInfoModel.add(authenticatedRoleMapping, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                PoddRoles.ADMIN.getURI());
        
        final ClientResource userAddClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.POST, input, mediaType,
                    Status.SUCCESS_OK, this.testWithAdminPrivileges);
            Assert.fail("Should have failed due to missing email");
        }
        catch(final ResourceException e)
        {
            // verify: the cause (simple string matching, not checking for valid RDF content)
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
            final String body = userAddClientResource.getResponseEntity().getText();
            Assert.assertTrue("Expected cause is missing", body.contains("User Email cannot be empty"));
        }
    }
    
    @Test
    public void testErrorAddDuplicateUserRdf() throws Exception
    {
        final String testIdentifier = "testuser@podd.com";

        // prepare: add a Test User account
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier, null, null,
                null, null, null, null, null, roles, PoddUserStatus.ACTIVE);
        
        // prepare: add another User account with same Identifier/email
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final Model userInfoModel = new LinkedHashModel();
        final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                PoddRdfConstants.VF.createLiteral(testIdentifier));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                PoddRdfConstants.VF.createLiteral("testpassword"));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral("First"));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral("Last"));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_HOMEPAGE,
                PoddRdfConstants.VF.createURI("http://nohomepage"));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORGANIZATION,
                PoddRdfConstants.VF.createLiteral("n/a"));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_ORCID, PoddRdfConstants.VF.createLiteral("n/a"));
        userInfoModel
                .add(tempUserUri, SesameRealmConstants.OAS_USEREMAIL, PoddRdfConstants.VF.createLiteral(testIdentifier));
        
        
        final ClientResource userAddClientResource = new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ADD));
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        try
        {
                RestletTestUtils.doTestAuthenticatedRequest(userAddClientResource, Method.POST, input, mediaType,
                        Status.CLIENT_ERROR_CONFLICT, this.testWithAdminPrivileges);
                Assert.fail("Should throw an exception because Identifier already used");
        }
        catch (ResourceException e)
        {
            // verify: the cause (simple string matching, not checking for valid RDF content)
            Assert.assertEquals(Status.CLIENT_ERROR_CONFLICT, e.getStatus());
            final String body = userAddClientResource.getResponseEntity().getText();
            System.out.println(body);
            Assert.assertTrue("Expected cause is missing", body.contains("User already exists"));
        }
    }
    
}
