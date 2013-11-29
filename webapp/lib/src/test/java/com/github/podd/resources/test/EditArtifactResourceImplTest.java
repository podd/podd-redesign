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

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

/**
 * 
 * @author kutila
 */
public class EditArtifactResourceImplTest extends AbstractResourceImplTest
{
    
    @Ignore
    @Test
    public void testEditArtifactBasicJson() throws Exception
    {
        Assert.fail("TODO: implement");
    }
    
    @Test
    public void testEditArtifactBasicRdf() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE,
                    Boolean.toString(false));
            // "force" query parameter is false by default
            
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                            MediaType.APPLICATION_RDF_XML);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = this.getText(results);
            
            // verify: Inferred Ontology ID is received in RDF format
            Assert.assertTrue("Response not in RDF format", body.contains("<rdf:RDF"));
            Assert.assertTrue("Artifact version has not been updated properly", body.contains("artifact:1:version:2"));
            Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
            Assert.assertTrue("Inferred version not in response", body.contains("inferredVersion"));
            
            // verify: publication46 has been added to the artifact
            final String artifactBody =
                    this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_XML);
            Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("Rice tree scan 003454-98"));
            Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("object-rice-scan-34343-a"));
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    @Test
    public void testEditArtifactBasicTurtle() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE,
                    Boolean.toString(false));
            // "force" query parameter is false by default
            
            // edit Representation contains statements in Turtle format
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_PUBLICATION_OBJECT,
                            MediaType.APPLICATION_RDF_TURTLE);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String updatedArtifactDetails = this.getText(results);
            
            // verify: Inferred Ontology ID is NOT in RDF format
            Assert.assertFalse("Response should not be in RDF format", updatedArtifactDetails.contains("<rdf:RDF"));
            Assert.assertTrue("Artifact version has not been updated properly",
                    updatedArtifactDetails.contains("artifact:1:version:2"));
            Assert.assertTrue("Version IRI not in response", updatedArtifactDetails.contains("versionIRI"));
            Assert.assertTrue("Inferred version not in response", updatedArtifactDetails.contains("inferredVersion"));
            
            // verify: publication46 has been added to the artifact
            final String artifactBody =
                    this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_TURTLE);
            Assert.assertTrue("New publication not added to artifact", artifactBody.contains("publication46"));
            // Assert.assertTrue("New publication not added to artifact",
            // artifactBody.contains("http://dx.doi.org/10.1109/eScience.2013.44"));
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    @Test
    public void testEditArtifactTurtleWithDanglingObjects() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            editArtifactClientResource
                    .addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(true));
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_FORCE, Boolean.toString(true));
            
            // edit Representation contains statements in Turtle format
            final Representation input =
                    this.buildRepresentationFromResource(
                            TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION,
                            MediaType.APPLICATION_RDF_TURTLE);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String updatedArtifactDetails = this.getText(results);
            
            // verify: Inferred Ontology ID is NOT in RDF format
            Assert.assertFalse("Response should not be in RDF format", updatedArtifactDetails.contains("<rdf:RDF"));
            Assert.assertTrue("Artifact version has not been updated properly",
                    updatedArtifactDetails.contains("artifact:1:version:2"));
            Assert.assertTrue("Version IRI not in response", updatedArtifactDetails.contains("versionIRI"));
            Assert.assertTrue("Inferred version not in response", updatedArtifactDetails.contains("inferredVersion"));
            
            // verify: objects left dangling after edit have been removed from
            // the artifact
            final String artifactBody =
                    this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_TURTLE);
            final String[] danglingObjects =
                    { "http://purl.org/podd/basic-2-20130206/artifact:1#SqueekeeMaterial",
                            "http://purl.org/podd/basic-2-20130206/artifact:1#Demo_genotype_3",
                            "http://purl.org/podd/basic-2-20130206/artifact:1#Sequence_A", };
            for(final String deletedObject : danglingObjects)
            {
                Assert.assertFalse("Dangling object still exists", artifactBody.contains(deletedObject));
            }
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    @Test
    public void testEditArtifactTurtleWithMultipleNewObjects() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            editArtifactClientResource
                    .addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(true));
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_FORCE, Boolean.toString(true));
            
            // prepare: add (temporary) object URIs that are being added
            final String[] newObjects =
                    { "urn:temp:uuid:object-rice-scan-34343-a", "urn:temp:uuid:publication35",
                            "urn:temp:uuid:publication46" };
            for(final String objectUri : newObjects)
            {
                editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectUri);
            }
            
            // edit Representation contains statements in Turtle format
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_MULTIPLE_OBJECTS_TTL,
                            MediaType.APPLICATION_RDF_TURTLE);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String updatedArtifactDetails = this.getText(results);
            
            // verify: Inferred Ontology ID is NOT in RDF format
            Assert.assertFalse("Response should not be in RDF format", updatedArtifactDetails.contains("<rdf:RDF"));
            Assert.assertTrue("Artifact version has not been updated properly",
                    updatedArtifactDetails.contains("artifact:1:version:2"));
            Assert.assertTrue("Version IRI not in response", updatedArtifactDetails.contains("versionIRI"));
            Assert.assertTrue("Inferred version not in response", updatedArtifactDetails.contains("inferredVersion"));
            
            // verify: response contains the ontology ID
            final Model model = Rio.parse(new StringReader(updatedArtifactDetails), "", RDFFormat.TURTLE);
            final Collection<InferredOWLOntologyID> updatedOntologyID = OntologyUtils.modelToOntologyIDs(model);
            Assert.assertEquals("Response did not contain an ontology ID", 1, updatedOntologyID.size());
            
            // verify: response contains object URIs and their PURLs
            for(final String objectUri : newObjects)
            {
                final String purl =
                        model.filter(PODD.VF.createURI(objectUri), PODD.PODD_REPLACED_TEMP_URI_WITH, null)
                                .objectString();
                Assert.assertNotNull("Object URI's PURL not in response", purl);
                Assert.assertTrue("PURL does not start as expected", purl.startsWith("http://example.org/purl/"));
            }
            
            final String artifactBody =
                    this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_TURTLE);
            // verify: publication46 has been added to the artifact
            Assert.assertTrue("New publication not added to artifact", artifactBody.contains("publication46"));
            // Assert.assertTrue("New publication not added to artifact",
            // artifactBody.contains("http://dx.doi.org/10.1109/eScience.2013.44"));
            
            // verify: publication46 has been added to the artifact
            Assert.assertTrue("New publication not added to artifact", artifactBody.contains("publication35"));
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    @Test
    public void testErrorEditArtifactRdfWithIncorrectArtifactID() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        // prepare: set an INVALID artifact IRI
        final String incorrectArtifactID = artifactID.getOntologyIRI().toString() + "_wrong";
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, incorrectArtifactID);
            
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            
            editArtifactClientResource
                    .addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(true));
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_FORCE, Boolean.toString(false));
            
            // create edit Representation
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                            MediaType.APPLICATION_RDF_XML);
            
            RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                    MediaType.APPLICATION_RDF_XML, Status.CLIENT_ERROR_NOT_FOUND, this.testWithAdminPrivileges);
            Assert.fail("Should have failed due to incorrect artifact IRI");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_NOT_FOUND, e.getStatus());
            
            // TODO: verify the cause and details (as in
            // UploadArtifactResourceImplTest)
            final String body = this.getText(editArtifactClientResource.getResponseEntity());
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
            final Model model = Rio.parse(inputStream, "", RDFFormat.RDFXML);
            
            final String sourceOfError = model.filter(null, PODD.ERR_SOURCE, null).objectString();
            Assert.assertEquals("Err#source is not the incorrect artifact ID", incorrectArtifactID, sourceOfError);
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    @Test
    public void testErrorEditArtifactRdfWithIncorrectVersionIRI() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        // prepare: set an INVALID version IRI
        final String incorrectVersionIri = artifactID.getVersionIRI().toString() + ":nosuchversion";
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER,
                    incorrectVersionIri);
            
            editArtifactClientResource
                    .addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(true));
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_FORCE, Boolean.toString(false));
            
            // create edit Representation
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                            MediaType.APPLICATION_RDF_XML);
            RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                    MediaType.APPLICATION_RDF_XML, Status.CLIENT_ERROR_CONFLICT, this.testWithAdminPrivileges);
            Assert.fail("Should have failed due to incorrect version IRI");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_CONFLICT, e.getStatus());
            
            // verify the source of error
            final String body = this.getText(editArtifactClientResource.getResponseEntity());
            final ByteArrayInputStream inputStream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
            final Model model = Rio.parse(inputStream, "", RDFFormat.RDFXML);
            
            final String sourceOfError = model.filter(null, PODD.ERR_SOURCE, null).objectString();
            Assert.assertEquals("Err#source is not the incorrect Version IRI", incorrectVersionIri, sourceOfError);
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    @Test
    public void testErrorEditArtifactRdfWithoutArtifactID() throws Exception
    {
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        // there is no need to authenticate, have a test artifact or send RDF to
        // modify as the
        // artifact is checked for first
        try
        {
            editArtifactClientResource.post(null, MediaType.TEXT_PLAIN);
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    @Test
    public void testErrorEditArtifactRdfWithoutAuthentication() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        // invoke without authentication
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactUri);
            
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                            MediaType.APPLICATION_RDF_XML);
            
            editArtifactClientResource.post(input, MediaType.APPLICATION_RDF_XML);
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    /**
     * NOTE: The expected 500 error causes a stacktrace to be printed on the server
     */
    @Test
    public void testErrorEditArtifactTurtleWithReportDanglingObjects() throws Exception
    {
        // prepare: add an artifact
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            editArtifactClientResource
                    .addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_REPLACE, Boolean.toString(true));
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_EDIT_WITH_FORCE, Boolean.toString(false));
            
            // edit Representation contains statements in Turtle format
            final Representation input =
                    this.buildRepresentationFromResource(
                            TestConstants.TEST_ARTIFACT_FRAGMENT_MODIFY_DEMO_INVESTIGATION,
                            MediaType.APPLICATION_RDF_TURTLE);
            
            RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.POST, input,
                    MediaType.APPLICATION_RDF_TURTLE, Status.SERVER_ERROR_INTERNAL, this.testWithAdminPrivileges);
            Assert.fail("Should have failed when dangling objects were identified");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.SERVER_ERROR_INTERNAL, e.getStatus());
            // the cause is not available to the client for verification
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    /**
     * Test viewing the edit HTML page for an internal PODD object.
     */
    @Test
    public void testGetEditArtifactHtmlForInternalObject() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact("/test/artifacts/basic-2.rdf");
        
        final String objectUri = "urn:hardcoded:purl:artifact:1#publication45";
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_OBJECT_IDENTIFIER, objectUri);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = this.getText(results);
            
            // verify:
            // System.out.println(body);
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
    /**
     * Test viewing the edit HTML page for a PODD top object (i.e. a Project).
     */
    @Test
    public void testGetEditArtifactHtmlForTopObject() throws Exception
    {
        // prepare: add an artifact
        final String artifactUri = this.loadTestArtifact("/test/artifacts/basic-2.rdf");
        
        final ClientResource editArtifactClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ARTIFACT_EDIT));
        
        try
        {
            editArtifactClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            // not requesting a specific object results in the Project being
            // shown.
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(editArtifactClientResource, Method.GET, null,
                            MediaType.TEXT_HTML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = this.getText(results);
            
            // verify:
            // System.out.println(body);
            this.assertFreemarker(body);
        }
        finally
        {
            this.releaseClient(editArtifactClientResource);
        }
    }
    
}
