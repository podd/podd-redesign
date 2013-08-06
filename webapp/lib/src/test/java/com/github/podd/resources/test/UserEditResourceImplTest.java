/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
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
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        System.out.println(body);
        this.assertFreemarker(body);
        
        Assert.assertTrue("Page missing User identifier", body.contains(testIdentifier));
        Assert.assertTrue("Page missing first name", body.contains("Test Admin"));
        Assert.assertTrue("Page missing last name", body.contains("User"));
        Assert.assertTrue("Page missing organization", body.contains("UQ"));
        Assert.assertTrue("Page missing home page", body.contains("http://www.example.com/testAdmin"));
        Assert.assertTrue("Page missing orcid", body.contains("Orcid-Test-Admin"));
    }
    
    /**
     * Verify that changing password using the Edit User Interface has no effect
     */
    @Test
    public void testErrorEditCurrentUserPasswordRdf() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final String testPassword = "modifiedPassword";
        final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:temp:user");

        // prepare: create Model with modified password and user identifier
        final Model userInfoModel = new LinkedHashModel();
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER,
                PoddRdfConstants.VF.createLiteral(testIdentifier));
        userInfoModel.add(tempUserUri, SesameRealmConstants.OAS_USERSECRET,
                PoddRdfConstants.VF.createLiteral(testPassword));
        userInfoModel.add(tempUserUri, PoddRdfConstants.PODD_USER_STATUS, PoddUserStatus.ACTIVE.getURI());
        
        // submit new password to Edit User Service
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: response has correct identifier
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        
        // verify: request with old login details should still succeed
        final ClientResource userDetailsClientResource2 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                    Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
        }
        catch(ResourceException e)
        {
            Assert.fail("Should have succeeded as password was not changed");
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
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model userInfoModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 15);
        // this.log.info("Retrieved [{}] details. ", testIdentifier);
        // DebugUtils.printContents(userInfoModel);
        
        
        // prepare: modify existing User's details
        final String modifiedFirstName = "Totally";
        final String modifiedLastName = "Newman";
        
        final Resource userUri =
                userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
        
        userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
        userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral(modifiedFirstName));
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral(modifiedLastName));
        
        
        // submit modified details to Edit User Service
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        
        // verify: response has correct identifier
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        
        
        // verify: details have been correctly updated (by retrieving User details again)
        final ClientResource userDetailsClientResource2 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 15);
        
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Unexpected user URI", userUri.stringValue(),
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next()
                        .stringValue());
        Assert.assertEquals("First name was not modified", modifiedFirstName,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERFIRSTNAME, null).objectString());
        Assert.assertEquals("Last name was not modified", modifiedLastName,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERLASTNAME, null).objectString());
        Assert.assertEquals("Role count should not have changed", 2,
                resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects().size());
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
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project")));
        final String testFirstName = "John";
        final String testLastName = "Doe";
        final String testOrganization = "CSIRO";
        final String testOrcid = "john-orcid";
        final String testTitle = "Mr";
        final String testPhone = "000333434";
        final String testAddress = "Some Address";
        final String testPosition = "Researcher";
        this.loadTestUser(testIdentifier, "testuserpassword", testFirstName, testLastName, testIdentifier,
                testHomePage, testOrganization, testOrcid, testTitle, testPhone, testAddress, testPosition,
                roles, PoddUserStatus.ACTIVE);
        
        
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        //System.out.println(body);
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
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        // prepare: retrieve Details of existing User
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model userInfoModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 16);
        // this.log.info("Retrieved [{}] details. ", testIdentifier);
        // DebugUtils.printContents(userInfoModel);
        
        // prepare: modify existing User's details
        final String modifiedFirstName = "Totally";
        final String modifiedLastName = "Newman";
        
        final Resource userUri =
                userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
        
        userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
        userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
        userInfoModel.remove(userUri, PoddRdfConstants.PODD_USER_STATUS, null);
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral(modifiedFirstName));
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral(modifiedLastName));
        userInfoModel.add(userUri, PoddRdfConstants.PODD_USER_STATUS, PoddUserStatus.INACTIVE.getURI());
        
        // submit modified details to Edit User Service
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        // verify: response has correct identifier
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        
        // verify: details have been correctly updated (by retrieving User details again)
        final ClientResource userDetailsClientResource2 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 16);
        
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Unexpected user URI", userUri.stringValue(),
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).subjects().iterator().next()
                        .stringValue());
        Assert.assertEquals("First name was not modified", modifiedFirstName,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERFIRSTNAME, null).objectString());
        Assert.assertEquals("Last name was not modified", modifiedLastName,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERLASTNAME, null).objectString());
        Assert.assertEquals("Role count should not have changed", 1,
                resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects().size());
        Assert.assertEquals("Status was not modified", PoddUserStatus.INACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
    }
    
    /**
     * Test authenticated edit of other user's Roles as Administrator
     */
    @Test
    public void testEditOtherUserRolesRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:cotton-leaf-morphology")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:tea-leaf-study")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        
        // prepare: retrieve Details of Test User
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model userInfoModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 20);
        final Literal userIdentifier = userInfoModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectLiteral();
        
        
        // prepare: modify Test User's Roles
        final URI tempUserUri = PoddRdfConstants.VF.createURI("urn:update:user:roles");
        final Model newModel = new LinkedHashModel();
        newModel.add(tempUserUri, SesameRealmConstants.OAS_USERIDENTIFIER, userIdentifier);
        newModel.add(tempUserUri, PoddRdfConstants.PODD_USER_STATUS, PoddUserStatus.ACTIVE.getURI());
        
        final URI testProjectUri = PoddRdfConstants.VF.createURI("urn:podd:test-project-23567");
        final URI roleMapping1Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, testProjectUri);

        final URI roleMapping2Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping2:", UUID.randomUUID().toString());
        newModel.add(roleMapping2Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDUSER, tempUserUri);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_ADMIN.getURI());
        newModel.add(roleMapping2Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, 
                PoddRdfConstants.VF.createURI("urn:podd:test-project-9999-paddy"));
        
        
        // submit modified details to Edit User Service
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(newModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());

        
        // verify: Test User Roles have been correctly updated
        final ClientResource userDetailsClientResource2 =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource2, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 20);
        
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
        final Set<Value> roleSet = resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, null).objects();
        Assert.assertEquals("Role count incorrect", 2, roleSet.size());
        Assert.assertTrue("Project Observer Role missing", roleSet.contains(PoddRoles.PROJECT_OBSERVER.getURI()));
        Assert.assertTrue("Project ADMIN Role missing", roleSet.contains(PoddRoles.PROJECT_ADMIN.getURI()));
        // note: above simple verification does not ensure that Roles are correctly linked with
        // objects/roles
    }

    
    /**
     * Test error trying to edit a User that does not exist in the system
     */
    @Test
    public void testErrorEditNonExistentUserRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        
        // submit modified details to Edit User Service
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + "noSuchUser"));
        
        final Representation input = new StringRepresentation("Should have user model in JSON", mediaType);
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                    Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(e.getStatus(), Status.CLIENT_ERROR_BAD_REQUEST);
        }
    }
    
    /**
     * Test error trying to display other user Edit page as non-admin user
     */
    @Test
    public void testErrorEditOtherUserNonAdminHtml() throws Exception
    {
        final String testIdentifier = "testAdminUser";
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
        
        try
        {
            RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.GET, null, MediaType.TEXT_HTML,
                    Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(ResourceException e)
        {
            Assert.assertEquals("Expected UNAUTHORIZED error", Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
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
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:some-project")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        // prepare: retrieve Details of existing User
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final Model userInfoModel =
                this.assertRdf(new ByteArrayInputStream(results.getText().getBytes(StandardCharsets.UTF_8)), format, 16);
        // this.log.info("Retrieved [{}] details. ", testIdentifier);
        // DebugUtils.printContents(userInfoModel);
        
        // prepare: modify existing User's details
        final String modifiedFirstName = "Totally";
        final String modifiedLastName = "Newman";
        
        final Resource userUri =
                userInfoModel.filter(null, SesameRealmConstants.OAS_USEREMAIL, null).subjects().iterator().next();
        
        userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERFIRSTNAME, null);
        userInfoModel.remove(userUri, SesameRealmConstants.OAS_USERLASTNAME, null);
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERFIRSTNAME,
                PoddRdfConstants.VF.createLiteral(modifiedFirstName));
        userInfoModel.add(userUri, SesameRealmConstants.OAS_USERLASTNAME,
                PoddRdfConstants.VF.createLiteral(modifiedLastName));
        
        // try to submit modified details to Edit User Service
        final ClientResource userEditClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(userInfoModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        try
        {
                RestletTestUtils.doTestAuthenticatedRequest(userEditClientResource, Method.POST, input, mediaType,
                        Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
                Assert.fail("Should have thrown a ResourceException due to lack of authorization");
        }
        catch (ResourceException e)
        {
            Assert.assertEquals("Should have been Unauthorized",  Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }
}
