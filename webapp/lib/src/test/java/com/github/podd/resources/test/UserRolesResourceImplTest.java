/**
 * 
 */
package com.github.podd.resources.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Collection;
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
import com.github.podd.api.test.TestConstants;
import com.github.podd.restlet.PoddRoles;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddUserStatus;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 */
public class UserRolesResourceImplTest extends AbstractResourceImplTest
{

    /**
     * Tests editing of a PODD User's Roles.  
     */
    @Test
    public void testEditUserRolesAddToOtherRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:cotton-leaf-morphology")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:tea-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:coffee-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:banana-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:coconut-leaf-study")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        
        // prepare: modify Test User's Roles
        final Model newModel = new LinkedHashModel();
        
        // prepare: additional Project Observer Role for Project "cotton-leaf-23567"
        final URI testProject1Uri = PoddRdfConstants.VF.createURI("urn:podd:project-cotton-leaf-23567");
        final URI roleMapping1Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, testProject1Uri);
        // NOTE: no need to specify ROLE_MAPPED_USER as User is identified from the request
        
        // prepare: additional Project Observer Role for Project "tea-leaf-99"
        final URI testProject2Uri = PoddRdfConstants.VF.createURI("urn:podd:project-tea-leaf-99");
        final URI roleMapping2Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping2:", UUID.randomUUID().toString());
        newModel.add(roleMapping2Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping2Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, testProject2Uri);
        
        
        // submit modified details to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_ROLES + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(newModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());

        
        // verify: Test User Roles have been correctly updated
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 43);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
        
        final Collection<Value> objects = resultsModel.filter(null, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, null).objects();
        Assert.assertEquals("Incorrect no. of Project Roles", 7, objects.size());
        
        final Collection<Resource> subjects =
                resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI())
                        .subjects();
        Assert.assertEquals("Incorrect no. of Project Observer Roles", 4, subjects.size());
    }    
    
    /**
     * Tests deleting a PODD User's Roles.  
     */
    @Test
    public void testEditUserRolesDeleteOtherRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final URI testProject1Uri = PoddRdfConstants.VF.createURI("urn:podd:coconut-leaf-study");
        final URI testProject2Uri = PoddRdfConstants.VF.createURI("urn:podd:banana-leaf-study");
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PoddRdfConstants.VF.createURI("urn:podd:cotton-leaf-morphology")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:tea-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PoddRdfConstants.VF.createURI("urn:podd:coffee-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), testProject2Uri));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), testProject1Uri));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        
        // prepare: Project Observer Role for Project "urn:podd:coconut-leaf-study" is to be deleted
        final URI roleMapping1Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        final Model newModel = new LinkedHashModel();
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, testProject1Uri);
        // NOTE: no need to specify ROLE_MAPPED_USER as User is identified from the request
        
        // submit details of Role to delete to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_ROLES + testIdentifier));
        userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_DELETE, "true");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(newModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        final Representation modifiedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model model =
                this.assertRdf(new ByteArrayInputStream(modifiedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        RDFFormat.RDFXML, 1);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());

        
        // verify: The Role has been correctly deleted
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 31);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
        
        final Set<Resource> observerMappings =
                resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI())
                        .subjects();
        Assert.assertEquals("Expected only 1 Project_Observer mapping", 1, observerMappings.size());
        
        final Resource mapping = (Resource)observerMappings.toArray()[0];
        Assert.assertEquals("Project_Observer Role is not for expected Project",
                testProject2Uri,
                resultsModel.filter(mapping, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, null).objectURI());
    }
    
    /**
     * Tests failure to add a PODD User's Roles.
     * 
     * A non-admin user attempts to add Project_Observer Role and Repository Admin Role to another test user.
     * Adding Admin Role is rejected as the user does not have enough privileges.
     * 
     * NOTE: This test expects that Role additions are attempted in the order: 
     *  - Project Observer (succeeds)
     *  - Admin (fails)
     * 
     * This order cannot be guaranteed and therefore the test could fail.
     */
    @Test
    public void testErrorEditUserRolesAddInsufficientPrivilegesRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_CREATOR.getURI(), null));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        
        // prepare: modify Test User's Roles
        final Model newModel = new LinkedHashModel();
        
        // prepare: additional Project Observer Role for TEST_ARTIFACT Project 
        final URI roleMapping1Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, PoddRdfConstants.TEST_ARTIFACT);
        // NOTE: no need to specify ROLE_MAPPED_USER as User is identified from the request
        
        // prepare: additional Repository Admin Role (which should fail)
        final URI roleMapping2Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping2:", UUID.randomUUID().toString());
        newModel.add(roleMapping2Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.ADMIN.getURI());
        
        // submit modified details to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_ROLES + testIdentifier));
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(newModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        try
        {
                RestletTestUtils.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                        Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
                Assert.fail("Should have failed authorization");
        }
        catch (ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        
        // verify: Test User Roles have been partially updated
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 19);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
        
        // verify: 2 Roles are mapped (an increase of 1)
        final Collection<Resource> roleMappings = resultsModel.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING).subjects();
        Assert.assertEquals("Incorrect no. of Project Roles", 2, roleMappings.size());
        
        // verify: Project Observer Role was successfully mapped
        final Collection<Resource> subjects =
                resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI())
                        .subjects();
        Assert.assertEquals("Incorrect no. of Project Observer Roles", 1, subjects.size());
    }
    
    
    /**
     * Tests removal of a PODD User's Roles. 
     */
    @Test
    public void testErrorEditUserRolesDeleteInsufficientPrivilegesRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final URI testProject1Uri = PoddRdfConstants.VF.createURI("urn:podd:coconut-leaf-study");
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PoddRdfConstants.TEST_ARTIFACT));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), testProject1Uri));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        // prepare: Project Observer Role for TEST_ARTIFACT is to be deleted
        final URI roleMapping1Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        final Model newModel = new LinkedHashModel();
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, PoddRdfConstants.TEST_ARTIFACT);

        // prepare: Project Observer Role for testProject1 is to be deleted
        final URI roleMapping2Uri =
                PoddRdfConstants.VF.createURI("urn:podd:rolemapping2:", UUID.randomUUID().toString());
        newModel.add(roleMapping2Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping2Uri, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, testProject1Uri);
        
        
        // submit details of Role to delete to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_ROLES + testIdentifier));
        userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_DELETE, "true");
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(newModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        try
        {
                RestletTestUtils.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                        Status.CLIENT_ERROR_UNAUTHORIZED, this.testNoAdminPrivileges);
                Assert.fail("Should have failed authorization");
        }
        catch (ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        
        
        // verify: The Role has been correctly deleted
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS + testIdentifier));
        final Representation updatedResults =
                RestletTestUtils.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                        Status.SUCCESS_OK, this.testWithAdminPrivileges);
        final Model resultsModel =
                this.assertRdf(new ByteArrayInputStream(updatedResults.getText().getBytes(StandardCharsets.UTF_8)),
                        format, 16);
        Assert.assertEquals("Unexpected user identifier", testIdentifier,
                resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
        Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                resultsModel.filter(null, PoddRdfConstants.PODD_USER_STATUS, null).objectURI());
        
        final Set<Resource> observerMappings =
                resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI())
                        .subjects();
        Assert.assertEquals("Expected only 1 Project_Observer mapping", 1, observerMappings.size());
        
        final Resource mapping = (Resource)observerMappings.toArray()[0];
        Assert.assertEquals("Project_Observer Role is not for expected Project",
                testProject1Uri,
                resultsModel.filter(mapping, PoddRdfConstants.PODD_ROLEMAPPEDOBJECT, null).objectURI());
    }

    @Test
    public void testUserRolesPageHtml() throws Exception
    {
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);

        //final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        //final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), artifactID.getOntologyIRI().toOpenRDFURI()));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), artifactID.getOntologyIRI().toOpenRDFURI()));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), artifactID.getOntologyIRI().toOpenRDFURI()));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        
        final ClientResource userEditRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_EDIT_ROLES + testIdentifier));
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(userEditRolesClientResource, Method.GET, null,
                        MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        System.out.println(body);
        this.assertFreemarker(body);
        
//        Assert.assertTrue("Page missing User identifier", body.contains(testIdentifier));
//        Assert.assertTrue("Page missing old password", body.contains("Old Password"));
//        Assert.assertTrue("Page missing confirm password", body.contains("Confirm New Password"));
//        Assert.assertTrue("Page missing save button", body.contains("Save Password"));
//        Assert.assertTrue("Page missing cancel button", body.contains("Cancel"));
    }
    
}
