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

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.impl.file.test.SSHService;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.OntologyUtils;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
// @Ignore("Tests fail sometimes with the SSH Service timing out due to lack of entropy. Should be run before releases.")
public class DataReferenceAttachResourceImplTest extends AbstractResourceImplTest
{
    /** SSH File Repository server for tests */
    protected SSHService sshd;
    
    private Path sshDir = null;
    
    /**
     * Given the path to a resource containing an incomplete File Reference object, this method
     * constructs a complete File Reference and returns it as an RDF/XML string.
     * 
     * @param fragmentSource
     *            Location of resource containing incomplete File Reference
     * @return String containing RDF statements
     */
    private String buildFileReferenceString(final String fragmentSource, final RDFFormat format,
            final Path testDirectory) throws Exception
    {
        // read the fragment's RDF statements into a Model
        final InputStream inputStream = this.getClass().getResourceAsStream(fragmentSource);
        final Model model = Rio.parse(inputStream, "", format);
        
        // path to be set as part of the file reference
        final Path completePath = testDirectory.resolve(TestConstants.TEST_REMOTE_FILE_NAME);
        Files.copy(
                this.getClass().getResourceAsStream(
                        TestConstants.TEST_REMOTE_FILE_PATH + "/" + TestConstants.TEST_REMOTE_FILE_NAME), completePath,
                StandardCopyOption.REPLACE_EXISTING);
        
        final Resource aliasUri =
                model.filter(null, PoddRdfConstants.PODD_BASE_HAS_ALIAS, null).subjects().iterator().next();
        model.add(aliasUri, PoddRdfConstants.PODD_BASE_HAS_FILE_PATH,
                ValueFactoryImpl.getInstance().createLiteral(testDirectory.toAbsolutePath().toString()));
        model.add(aliasUri, PoddRdfConstants.PODD_BASE_HAS_FILENAME,
                ValueFactoryImpl.getInstance().createLiteral(TestConstants.TEST_REMOTE_FILE_NAME));
        
        // get a String representation of the statements in the Model
        final StringWriter out = new StringWriter();
        Rio.write(model, out, format);
        
        return out.toString();
    }
    
    /**
     * Override this to change the test aliases for a given test.
     * 
     * @return A {@link Model} containing the statements relevant to test aliases.
     * @throws IOException
     * @throws UnsupportedRDFormatException
     * @throws RDFParseException
     */
    @Override
    protected Model getTestAliases() throws RDFParseException, UnsupportedRDFormatException, IOException
    {
        String configuration =
                IOUtils.toString(this.getClass().getResourceAsStream("/test/test-alias.ttl"), StandardCharsets.UTF_8);
        
        configuration = configuration.replace("9856", Integer.toString(this.sshd.TEST_SSH_SERVICE_PORT));
        
        return Rio.parse(new StringReader(configuration), "", RDFFormat.TURTLE);
    }
    
    @Before
    @Override
    public void setUp() throws Exception
    {
        this.sshDir = this.tempDirectory.newFolder("podd-filerepository-manager-impl-test").toPath();
        this.sshd = new SSHService();
        this.sshd.startTestSSHServer(this.sshDir);
        super.setUp();
    }
    
    protected void startRepositorySource() throws Exception
    {
    }
    
    protected void stopRepositorySource() throws Exception
    {
        if(this.sshd != null)
        {
            this.sshd.stopTestSSHServer(this.sshDir);
        }
    }
    
    /**
     * Test successful attach of a file reference in RDF/XML
     */
    @Test
    public void testAttachFileReferenceRdfWithoutVerification() throws Exception
    {
        // prepare: add an artifact (with PURLs so where to attach FileRef is
        // known in advance)
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
        
        try
        {
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            // Query parameter Verification policy - NOT SUPPLIED - defaults to
            // false
            
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                            MediaType.APPLICATION_RDF_XML);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final Model ontologyIDModel = this.assertRdf(results, RDFFormat.RDFXML, 3);
            
            Assert.assertEquals(2, ontologyIDModel.subjects().size());
            Assert.assertEquals(2, ontologyIDModel.predicates().size());
            Assert.assertEquals(2, ontologyIDModel.objects().size());
            Assert.assertEquals(1, ontologyIDModel.contexts().size());
            
            final List<InferredOWLOntologyID> ontologyIDs = OntologyUtils.modelToOntologyIDs(ontologyIDModel);
            Assert.assertEquals(1, ontologyIDs.size());
            
            final Model artifactModel = this.getArtifactAsModel(artifactID.getOntologyIRI().toString());
            
            DebugUtils.printContents(artifactModel);
            
            Assert.assertEquals(98, artifactModel.size());
            Assert.assertEquals(20, artifactModel.subjects().size());
            Assert.assertEquals(33, artifactModel.predicates().size());
            Assert.assertEquals(74, artifactModel.objects().size());
            Assert.assertEquals(1, artifactModel.contexts().size());
            
            // final String body = this.getText(results);
            
            // verify: Inferred Ontology ID is received in RDF format
            // Assert.assertTrue("Response not in RDF format",
            // body.contains("<rdf:RDF"));
            // Assert.assertTrue("Artifact version has not been updated properly",
            // body.contains("artifact:1:version:2"));
            // Assert.assertTrue("Version IRI not in response",
            // body.contains("versionIRI"));
            // Assert.assertTrue("Inferred version not in response",
            // body.contains("inferredVersion"));
            //
            // // verify: new file reference has been added to the artifact
            // final String artifactBody =
            // Assert.assertTrue("New file ref not added to artifact",
            // artifactBody.contains("Rice tree scan 003454-98"));
            // Assert.assertTrue("New file ref not added to artifact",
            // artifactBody.contains("object-rice-scan-34343-a"));
        }
        finally
        {
            this.releaseClient(fileRefAttachClientResource);
        }
    }
    
    /**
     * Test successful attach of a file reference in RDF/XML with verification. This test starts up
     * an SSH server as a File Repository source and can be slow to complete.
     */
    @Test
    public void testAttachFileReferenceRdfWithVerification() throws Exception
    {
        this.startRepositorySource();
        
        try
        {
            // prepare: add an artifact (with PURLs so where to attach FileRef
            // is known in advance)
            final InferredOWLOntologyID artifactID =
                    this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
            
            // prepare: build test fragment with correct value set for
            // poddBase:hasPath
            final String fileReferenceAsString =
                    this.buildFileReferenceString(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_VERIFIABLE,
                            RDFFormat.RDFXML, this.sshDir);
            Assert.assertFalse("Input DataReference could not be genereated", fileReferenceAsString.isEmpty());
            
            final ClientResource fileRefAttachClientResource =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
            
            try
            {
                fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                        .getOntologyIRI().toString());
                fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER,
                        artifactID.getVersionIRI().toString());
                fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY,
                        Boolean.toString(true));
                
                final Representation input =
                        new StringRepresentation(fileReferenceAsString, MediaType.APPLICATION_RDF_XML);
                
                final Representation results =
                        RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                                MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
                
                final String body = this.getText(results);
                
                // verify: Inferred Ontology ID is received in RDF format
                Assert.assertTrue("Response not in RDF format", body.contains("<rdf:RDF"));
                Assert.assertTrue("Artifact version has not been updated properly",
                        body.contains("artifact:1:version:2"));
                Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
                Assert.assertFalse("Inferred version in response", body.contains("inferredVersion"));
                
                // verify: new file reference has been added to the artifact
                final String artifactBody =
                        this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_XML);
                Assert.assertTrue("New file ref not added to artifact",
                        artifactBody.contains("Rice tree scan 003454-98"));
                Assert.assertTrue("New file ref not added to artifact",
                        artifactBody.contains("object-rice-scan-34343-a"));
            }
            finally
            {
                this.releaseClient(fileRefAttachClientResource);
            }
        }
        finally
        {
            this.stopRepositorySource();
        }
    }
    
    /**
     * Test successful attach of a file reference in Turtle
     */
    @Test
    public void testAttachFileReferenceTurtleWithoutVerification() throws Exception
    {
        // prepare: add an artifact (with PURLs so where to attach FileRef is
        // known in advance)
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
        
        try
        {
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            // Query parameter Verification policy - NOT SUPPLIED - defaults to
            // false
            
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT_TTL,
                            MediaType.APPLICATION_RDF_TURTLE);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = this.getText(results);
            
            // verify: An updated Inferred Ontology ID is received
            Assert.assertTrue("Artifact version has not been updated properly", body.contains("artifact:1:version:2"));
            Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
            Assert.assertFalse("Inferred version in response", body.contains("inferredVersion"));
            
            // verify: new file reference has been added to the artifact
            final String artifactBody =
                    this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_XML);
            Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("Rice tree scan 003454-98"));
            Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("object-rice-scan-34343-a"));
        }
        finally
        {
            this.releaseClient(fileRefAttachClientResource);
        }
    }
    
    /**
     * Test successful attach of a file reference in Turtle with verification. This test starts up
     * an SSH server as a File Repository source and can be slow to complete.
     */
    @Test
    public void testAttachFileReferenceTurtleWithVerification() throws Exception
    {
        this.startRepositorySource();
        
        try
        {
            // prepare: add an artifact (with PURLs so where to attach FileRef
            // is known in advance)
            final InferredOWLOntologyID artifactID =
                    this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
            
            // prepare: build test fragment with correct value set for
            // poddBase:hasPath
            final String fileReferenceAsString =
                    this.buildFileReferenceString(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_VERIFIABLE_TTL,
                            RDFFormat.TURTLE, this.sshDir);
            Assert.assertFalse("Input DataReference could not be genereated", fileReferenceAsString.isEmpty());
            
            final ClientResource fileRefAttachClientResource =
                    new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
            
            try
            {
                fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                        .getOntologyIRI().toString());
                fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER,
                        artifactID.getVersionIRI().toString());
                fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY,
                        Boolean.toString(true));
                
                final Representation input =
                        new StringRepresentation(fileReferenceAsString, MediaType.APPLICATION_RDF_TURTLE);
                
                final Representation results =
                        RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                                MediaType.APPLICATION_RDF_TURTLE, Status.SUCCESS_OK, this.testWithAdminPrivileges);
                
                final String body = this.getText(results);
                
                // verify: Inferred Ontology ID is received in RDF format
                Assert.assertTrue("Artifact version has not been updated properly",
                        body.contains("artifact:1:version:2"));
                Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
                Assert.assertFalse("Inferred version in response", body.contains("inferredVersion"));
                
                // verify: new file reference has been added to the artifact
                final String artifactBody =
                        this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_XML);
                Assert.assertTrue("New file ref not added to artifact",
                        artifactBody.contains("Rice tree scan 003454-98"));
                Assert.assertTrue("New file ref not added to artifact",
                        artifactBody.contains("object-rice-scan-34343-a"));
            }
            finally
            {
                this.releaseClient(fileRefAttachClientResource);
            }
        }
        finally
        {
            this.stopRepositorySource();
        }
    }
    
    /**
     * Test attach a file reference which fails verification in RDF/XML.
     * 
     * NOTE: This test causes Restlet to print a stack trace when the "502 Bad Gateway" Exception is
     * thrown.
     */
    @Test
    public void testErrorAttachFileReferenceRdfFileVerificationFailure() throws Exception
    {
        // prepare: add an artifact (with PURLs so where to attach FileRef is
        // known in advance)
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                        MediaType.APPLICATION_RDF_XML);
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
        
        try
        {
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                    .getOntologyIRI().toString());
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                    .getVersionIRI().toString());
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY,
                    Boolean.toString(true));
            
            RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                    MediaType.APPLICATION_RDF_XML, Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.SERVER_ERROR_BAD_GATEWAY, e.getStatus());
            final Representation responseEntity = fileRefAttachClientResource.getResponseEntity();
            Assert.assertTrue(this.getText(responseEntity).contains("File Reference validation resulted in failures"));
        }
        finally
        {
            this.releaseClient(fileRefAttachClientResource);
        }
    }
    
    @Test
    public void testErrorAttachFileReferenceRdfWithoutArtifactID() throws Exception
    {
        // prepare: dummy artifact details
        final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
        
        this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                MediaType.APPLICATION_RDF_XML);
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
        
        try
        {
            // Query parameter Artifact ID - NOT SUPPLIED
            fileRefAttachClientResource
                    .addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactUri);
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY,
                    Boolean.toString(true));
            
            // there is no need to authenticate, have a test artifact or send
            // RDF content as the
            // artifact ID is checked for first
            
            fileRefAttachClientResource.post(null, MediaType.TEXT_PLAIN);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
        finally
        {
            this.releaseClient(fileRefAttachClientResource);
        }
    }
    
    /**
     * Test attach a file reference without authentication
     */
    @Test
    public void testErrorAttachFileReferenceRdfWithoutAuthentication() throws Exception
    {
        // prepare: dummy artifact details
        final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                        MediaType.APPLICATION_RDF_XML);
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
        
        // invoke without authentication
        try
        {
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            fileRefAttachClientResource
                    .addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactUri);
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY,
                    Boolean.toString(true));
            
            fileRefAttachClientResource.post(input, MediaType.APPLICATION_RDF_XML);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
        finally
        {
            this.releaseClient(fileRefAttachClientResource);
        }
    }
    
    @Test
    public void testErrorAttachFileReferenceRdfWithoutVersionIRI() throws Exception
    {
        // prepare: dummy artifact details
        final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                        MediaType.APPLICATION_RDF_XML);
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_DATA_REF));
        
        // authentication is required as version IRI is checked AFTER
        // authentication
        try
        {
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
            // Query parameter Version IRI - NOT SUPPLIED
            fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY,
                    Boolean.toString(true));
            
            RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                    MediaType.APPLICATION_RDF_XML, Status.CLIENT_ERROR_BAD_REQUEST, this.testWithAdminPrivileges);
            Assert.fail("Should have thrown a ResourceException");
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
        finally
        {
            this.releaseClient(fileRefAttachClientResource);
        }
    }
    
}
