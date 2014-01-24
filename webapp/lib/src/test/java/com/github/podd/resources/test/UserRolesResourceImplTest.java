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
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRoles;
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
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PODD.VF
                .createURI("urn:podd:cotton-leaf-morphology")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PODD.VF
                .createURI("urn:podd:tea-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PODD.VF
                .createURI("urn:podd:coffee-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PODD.VF
                .createURI("urn:podd:banana-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PODD.VF
                .createURI("urn:podd:coconut-leaf-study")));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        // prepare: modify Test User's Roles
        final Model newModel = new LinkedHashModel();
        
        // prepare: additional Project Observer Role for Project
        // "cotton-leaf-23567"
        final URI testProject1Uri = PODD.VF.createURI("urn:podd:project-cotton-leaf-23567");
        final URI roleMapping1Uri = PODD.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PODD.PODD_ROLEMAPPEDOBJECT, testProject1Uri);
        // NOTE: no need to specify ROLE_MAPPED_USER as User is identified from
        // the request
        
        // prepare: additional Project Observer Role for Project "tea-leaf-99"
        final URI testProject2Uri = PODD.VF.createURI("urn:podd:project-tea-leaf-99");
        final URI roleMapping2Uri = PODD.VF.createURI("urn:podd:rolemapping2:", UUID.randomUUID().toString());
        newModel.add(roleMapping2Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping2Uri, PODD.PODD_ROLEMAPPEDOBJECT, testProject2Uri);
        
        // submit modified details to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        try
        {
            userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rio.write(newModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            final Representation modifiedResults =
                    this.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            final Model model = this.assertRdf(modifiedResults, RDFFormat.RDFXML, 1);
            Assert.assertEquals("Unexpected user identifier", testIdentifier,
                    model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
            
            // verify: Test User Roles have been correctly updated
            final ClientResource userDetailsClientResource =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
            try
            {
                userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                
                final Representation updatedResults =
                        this.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                                Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
                final Model resultsModel = this.assertRdf(updatedResults, format, 43);
                Assert.assertEquals("Unexpected user identifier", testIdentifier,
                        resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
                Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                        resultsModel.filter(null, PODD.PODD_USER_STATUS, null).objectURI());
                
                final Collection<Value> objects = resultsModel.filter(null, PODD.PODD_ROLEMAPPEDOBJECT, null).objects();
                Assert.assertEquals("Incorrect no. of Project Roles", 7, objects.size());
                
                final Collection<Resource> subjects =
                        resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                                PoddRoles.PROJECT_OBSERVER.getURI()).subjects();
                Assert.assertEquals("Incorrect no. of Project Observer Roles", 4, subjects.size());
            }
            finally
            {
                this.releaseClient(userDetailsClientResource);
            }
        }
        finally
        {
            this.releaseClient(userRolesClientResource);
        }
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
        final URI testProject1Uri = PODD.VF.createURI("urn:podd:coconut-leaf-study");
        final URI testProject2Uri = PODD.VF.createURI("urn:podd:banana-leaf-study");
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), PODD.VF
                .createURI("urn:podd:cotton-leaf-morphology")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PODD.VF
                .createURI("urn:podd:tea-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), PODD.VF
                .createURI("urn:podd:coffee-leaf-study")));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), testProject2Uri));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), testProject1Uri));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        // prepare: Project Observer Role for Project
        // "urn:podd:coconut-leaf-study" is to be deleted
        final URI roleMapping1Uri = PODD.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        final Model newModel = new LinkedHashModel();
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PODD.PODD_ROLEMAPPEDOBJECT, testProject1Uri);
        // NOTE: no need to specify ROLE_MAPPED_USER as User is identified from
        // the request
        
        // submit details of Role to delete to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        try
        {
            userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_DELETE, "true");
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rio.write(newModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            final Representation modifiedResults =
                    this.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            final Model model = this.assertRdf(modifiedResults, RDFFormat.RDFXML, 1);
            Assert.assertEquals("Unexpected user identifier", testIdentifier,
                    model.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
            
            // verify: The Role has been correctly deleted
            final ClientResource userDetailsClientResource =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
            try
            {
                userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                
                final Representation updatedResults =
                        this.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                                Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
                final Model resultsModel = this.assertRdf(updatedResults, format, 31);
                Assert.assertEquals("Unexpected user identifier", testIdentifier,
                        resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
                Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                        resultsModel.filter(null, PODD.PODD_USER_STATUS, null).objectURI());
                
                final Set<Resource> observerMappings =
                        resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                                PoddRoles.PROJECT_OBSERVER.getURI()).subjects();
                Assert.assertEquals("Expected only 1 Project_Observer mapping", 1, observerMappings.size());
                
                final Resource mapping = (Resource)observerMappings.toArray()[0];
                Assert.assertEquals("Project_Observer Role is not for expected Project", testProject2Uri, resultsModel
                        .filter(mapping, PODD.PODD_ROLEMAPPEDOBJECT, null).objectURI());
            }
            finally
            {
                this.releaseClient(userDetailsClientResource);
            }
        }
        finally
        {
            this.releaseClient(userRolesClientResource);
        }
    }
    
    /**
     * Tests failure to add a PODD User's Roles.
     * 
     * A non-admin user attempts to add Project_Observer Role and Repository Admin Role to another
     * test user. Request is rejected as user does not have enough privileges to add Repository
     * Admin Role.
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
        final URI roleMapping1Uri = PODD.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PODD.PODD_ROLEMAPPEDOBJECT, PODD.TEST_ARTIFACT);
        // NOTE: no need to specify ROLE_MAPPED_USER as User is identified from
        // the request
        
        // prepare: additional Repository Admin Role (which should fail)
        final URI roleMapping2Uri = PODD.VF.createURI("urn:podd:rolemapping2:", UUID.randomUUID().toString());
        newModel.add(roleMapping2Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.ADMIN.getURI());
        
        // submit modified details to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        try
        {
            userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            Rio.write(newModel, out, format);
            final Representation input = new StringRepresentation(out.toString(), mediaType);
            
            try
            {
                this.logout();
                this.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                        Status.CLIENT_ERROR_UNAUTHORIZED, AbstractResourceImplTest.NO_ADMIN);
                Assert.fail("Should have failed authorization");
            }
            catch(final ResourceException e)
            {
                Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
            }
            
            // verify: No changes to mapped Roles
            final ClientResource userDetailsClientResource =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
            try
            {
                userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
                
                final Representation updatedResults =
                        this.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                                Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
                final Model resultsModel = this.assertRdf(updatedResults, format, 15);
                Assert.assertEquals("Unexpected user identifier", testIdentifier,
                        resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
                Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                        resultsModel.filter(null, PODD.PODD_USER_STATUS, null).objectURI());
                
                // verify: 1 Roles mapped
                final Collection<Resource> roleMappings =
                        resultsModel.filter(null, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING).subjects();
                Assert.assertEquals("Incorrect no. of Project Roles", 1, roleMappings.size());
                
                // verify: Project Creator Role still exists
                final Collection<Resource> subjects =
                        resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                                PoddRoles.PROJECT_CREATOR.getURI()).subjects();
                Assert.assertEquals("Incorrect no. of Project Creator Roles", 1, subjects.size());
            }
            finally
            {
                this.releaseClient(userDetailsClientResource);
            }
        }
        finally
        {
            this.releaseClient(userRolesClientResource);
        }
    }
    
    /**
     * Tests failure to remove a PODD User's Roles.
     * 
     * A non-admin user attempts to remove Project_Observer Roles from another test user. Request is
     * rejected as user does not have privileges to modify Roles of one of the Projects.
     */
    @Test
    public void testErrorEditUserRolesDeleteInsufficientPrivilegesRdf() throws Exception
    {
        final MediaType mediaType = MediaType.APPLICATION_RDF_XML;
        final RDFFormat format = Rio.getWriterFormatForMIMEType(mediaType.getName(), RDFFormat.RDFXML);
        
        // prepare: add a Test User account
        final URI testProject1Uri = PODD.VF.createURI("urn:podd:coconut-leaf-study");
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), PODD.TEST_ARTIFACT));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), testProject1Uri));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        // prepare: Project Observer Role for TEST_ARTIFACT is to be deleted
        final URI roleMapping1Uri = PODD.VF.createURI("urn:podd:rolemapping1:", UUID.randomUUID().toString());
        final Model newModel = new LinkedHashModel();
        newModel.add(roleMapping1Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping1Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping1Uri, PODD.PODD_ROLEMAPPEDOBJECT, PODD.TEST_ARTIFACT);
        
        // prepare: Project Observer Role for testProject1 is to be deleted
        final URI roleMapping2Uri = PODD.VF.createURI("urn:podd:rolemapping2:", UUID.randomUUID().toString());
        newModel.add(roleMapping2Uri, RDF.TYPE, SesameRealmConstants.OAS_ROLEMAPPING);
        newModel.add(roleMapping2Uri, SesameRealmConstants.OAS_ROLEMAPPEDROLE, PoddRoles.PROJECT_OBSERVER.getURI());
        newModel.add(roleMapping2Uri, PODD.PODD_ROLEMAPPEDOBJECT, testProject1Uri);
        
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        Rio.write(newModel, out, format);
        final Representation input = new StringRepresentation(out.toString(), mediaType);
        
        // submit details of Role to delete to User Roles Service
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        try
        {
            userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_DELETE, "true");
            
            this.logout();
            this.doTestAuthenticatedRequest(userRolesClientResource, Method.POST, input, mediaType,
                    Status.CLIENT_ERROR_UNAUTHORIZED, AbstractResourceImplTest.NO_ADMIN);
            Assert.fail("Should have failed authorization");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(userRolesClientResource);
        }
        
        // verify: No change to Roles
        final ClientResource userDetailsClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_DETAILS));
        try
        {
            userDetailsClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation updatedResults =
                    this.doTestAuthenticatedRequest(userDetailsClientResource, Method.GET, null, mediaType,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            final Model resultsModel = this.assertRdf(updatedResults, format, 20);
            Assert.assertEquals("Unexpected user identifier", testIdentifier,
                    resultsModel.filter(null, SesameRealmConstants.OAS_USERIDENTIFIER, null).objectString());
            Assert.assertEquals("Status was not ACTIVE", PoddUserStatus.ACTIVE.getURI(),
                    resultsModel.filter(null, PODD.PODD_USER_STATUS, null).objectURI());
            
            final Set<Resource> observerMappings =
                    resultsModel.filter(null, SesameRealmConstants.OAS_ROLEMAPPEDROLE,
                            PoddRoles.PROJECT_OBSERVER.getURI()).subjects();
            Assert.assertEquals("Project_Observer mappings have changed", 2, observerMappings.size());
        }
        finally
        {
            this.releaseClient(userDetailsClientResource);
        }
    }
    
    @Test
    public void testUserRolesPageHtmlNoIdentifier() throws Exception
    {
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        
        try
        {
            final Representation results =
                    this.doTestAuthenticatedRequest(userRolesClientResource, Method.GET, null, MediaType.TEXT_HTML,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = results.getText();
            System.out.println(body);
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(userRolesClientResource);
        }
    }
    
    @Test
    public void testUserRolesPageHtmlWithIdentifier() throws Exception
    {
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        // prepare: add a Test User account
        final String testIdentifier = "testuser@podd.com";
        final List<Map.Entry<URI, URI>> roles = new LinkedList<Map.Entry<URI, URI>>();
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.ADMIN.getURI(), null));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_ADMIN.getURI(), artifactID.getOntologyIRI()
                .toOpenRDFURI()));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_MEMBER.getURI(), artifactID.getOntologyIRI()
                .toOpenRDFURI()));
        roles.add(new AbstractMap.SimpleEntry<URI, URI>(PoddRoles.PROJECT_OBSERVER.getURI(), artifactID
                .getOntologyIRI().toOpenRDFURI()));
        this.loadTestUser(testIdentifier, "testuserpassword", "John", "Doe", testIdentifier,
                "http:///www.john.doe.com", "CSIRO", "john-orcid", "Mr", "000333434", "Some Address", "Researcher",
                roles, PoddUserStatus.ACTIVE);
        
        final ClientResource userRolesClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_USER_ROLES));
        try
        {
            userRolesClientResource.addQueryParameter(PoddWebConstants.KEY_USER_IDENTIFIER, testIdentifier);
            
            final Representation results =
                    this.doTestAuthenticatedRequest(userRolesClientResource, Method.GET, null, MediaType.TEXT_HTML,
                            Status.SUCCESS_OK, AbstractResourceImplTest.WITH_ADMIN);
            
            final String body = results.getText();
            System.out.println(body);
            this.assertFreemarker(body);
            
            // Assert.assertTrue("Page missing User identifier",
            // body.contains(testIdentifier));
            // Assert.assertTrue("Page missing old password",
            // body.contains("Old Password"));
            // Assert.assertTrue("Page missing confirm password",
            // body.contains("Confirm New Password"));
            // Assert.assertTrue("Page missing save button",
            // body.contains("Save Password"));
            // Assert.assertTrue("Page missing cancel button",
            // body.contains("Cancel"));
        }
        finally
        {
            this.releaseClient(userRolesClientResource);
        }
    }
    
}
