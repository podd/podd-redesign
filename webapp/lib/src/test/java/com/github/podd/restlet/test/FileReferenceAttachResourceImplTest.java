/**
 * 
 */
package com.github.podd.restlet.test;

import java.io.File;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import com.github.ansell.restletutils.test.RestletTestUtils;
import com.github.podd.api.test.TestConstants;
import com.github.podd.impl.file.test.SSHService;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class FileReferenceAttachResourceImplTest extends AbstractResourceImplTest
{
    @Rule
    public final TemporaryFolder tempDirectory = new TemporaryFolder();

    /** SSH File Repository server for tests */
    protected SSHService sshd;
    
    protected void startRepositorySource() throws Exception
    {
        sshd = new SSHService();
        final File tempDirForHostKey = this.tempDirectory.newFolder();
        sshd.startTestSSHServer(Integer.parseInt(SSHService.TEST_SSH_SERVICE_PORT),
                tempDirForHostKey);
    }

    protected void stopRepositorySource() throws Exception
    {
        if (sshd != null)
        {
            sshd.stopTestSSHServer();
        }
    }

    
    

    /**
     * Test successful attach of a file reference in RDF/XML
     */
    @Test
    public void testAttachFileReferenceRdfWithoutVerification() throws Exception
    {
        // prepare: add an artifact 
        // (use one with PURLs so that where to attach the file reference is known in advance)
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));

        
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                .getOntologyIRI().toString());
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                .getVersionIRI().toString());
        // Query parameter Verification policy - NOT SUPPLIED - defaults to false
        // fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(false));
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                        MediaType.APPLICATION_RDF_XML);
        
        final Representation results =
                RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                        MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
        
        final String body = results.getText();
        
        // verify: Inferred Ontology ID is received in RDF format
        Assert.assertTrue("Response not in RDF format", body.contains("<rdf:RDF"));
        Assert.assertTrue("Artifact version has not been updated properly", body.contains("artifact:1:version:2"));
        Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
        Assert.assertTrue("Inferred version not in response", body.contains("inferredVersion"));
        
        // verify: new file reference has been added to the artifact
        final String artifactBody =
                this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_XML);
        Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("Rice tree scan 003454-98"));
        Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("object-rice-scan-34343-a"));
    }
    
    /**
     * Test successful attach of a file reference in RDF/XML
     * TODO: setup file repositories before test
     */
    @Ignore 
    @Test
    public void testAttachFileReferenceRdfWithVerification() throws Exception
    {
        this.startRepositorySource();
        
        // prepare: add an artifact 
        // (use one with PURLs so that where to attach the file reference is known in advance)
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));

        
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                .getOntologyIRI().toString());
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                .getVersionIRI().toString());
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
        
        try
        {
            final Representation input =
                    this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_VERIFIABLE,
                            MediaType.APPLICATION_RDF_XML);
            
            final Representation results =
                    RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                            MediaType.APPLICATION_RDF_XML, Status.SUCCESS_OK, this.testWithAdminPrivileges);
            
            final String body = results.getText();
            
            // verify: Inferred Ontology ID is received in RDF format
            Assert.assertTrue("Response not in RDF format", body.contains("<rdf:RDF"));
            Assert.assertTrue("Artifact version has not been updated properly", body.contains("artifact:1:version:2"));
            Assert.assertTrue("Version IRI not in response", body.contains("versionIRI"));
            Assert.assertTrue("Inferred version not in response", body.contains("inferredVersion"));
            
            // verify: new file reference has been added to the artifact
            final String artifactBody =
                    this.getArtifactAsString(artifactID.getOntologyIRI().toString(), MediaType.APPLICATION_RDF_XML);
            Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("Rice tree scan 003454-98"));
            Assert.assertTrue("New file ref not added to artifact", artifactBody.contains("object-rice-scan-34343-a"));
        }
        finally
        {
            this.stopRepositorySource();
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
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
        
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactUri);
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                        MediaType.APPLICATION_RDF_XML);
        
        // invoke without authentication
        try
        {
            fileRefAttachClientResource.post(input, MediaType.APPLICATION_RDF_XML);
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, e.getStatus());
        }
    }

    @Test
    public void testErrorAttachFileReferenceRdfWithoutArtifactID() throws Exception
    {
        // prepare: dummy artifact details
        final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
        
        // Query parameter Artifact ID - NOT SUPPLIED
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactUri);
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                        MediaType.APPLICATION_RDF_XML);
        
        // there is no need to authenticate, have a test artifact or send RDF content as the
        // artifact ID is checked for first
        try
        {
            fileRefAttachClientResource.post(null, MediaType.TEXT_PLAIN);
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
    }
    
    @Test
    public void testErrorAttachFileReferenceRdfWithoutVersionIRI() throws Exception
    {
        // prepare: dummy artifact details
        final String artifactUri = "urn:purl:dummy:artifact:uri:artifact:1";
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
        
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactUri);
        // Query parameter Version IRI - NOT SUPPLIED
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                        MediaType.APPLICATION_RDF_XML);
        
        // authentication is required as version IRI is checked AFTER authentication
        try
        {
                RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                        MediaType.APPLICATION_RDF_XML, Status.SERVER_ERROR_INTERNAL, this.testWithAdminPrivileges);
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.CLIENT_ERROR_BAD_REQUEST, e.getStatus());
        }
    }

    
    /**
     * Test attach a file reference which fails verification in RDF/XML
     */
    @Ignore
    @Test
    public void testErrorAttachFileReferenceRdfFileVerificationFailure() throws Exception
    {
        // prepare: add an artifact 
        // (use one with PURLs so that where to attach the file reference is known in advance)
        final InferredOWLOntologyID artifactID =
                this.loadTestArtifact(TestConstants.TEST_ARTIFACT_20130206, MediaType.APPLICATION_RDF_TURTLE);
        
        
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));

        
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_IDENTIFIER, artifactID
                .getOntologyIRI().toString());
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_ARTIFACT_VERSION_IDENTIFIER, artifactID
                .getVersionIRI().toString());
        fileRefAttachClientResource.addQueryParameter(PoddWebConstants.KEY_VERIFICATION_POLICY, Boolean.toString(true));
        
        final Representation input =
                this.buildRepresentationFromResource(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT,
                        MediaType.APPLICATION_RDF_XML);
        
        try
        {
                RestletTestUtils.doTestAuthenticatedRequest(fileRefAttachClientResource, Method.POST, input,
                        MediaType.APPLICATION_RDF_XML, Status.SERVER_ERROR_INTERNAL, this.testWithAdminPrivileges);
        }
        catch(final ResourceException e)
        {
            Assert.assertEquals(Status.SERVER_ERROR_INTERNAL, e.getStatus());
            System.out.println("44444444444444 " + e.getCause().getMessage());
        }
    }

    
    /**
     * Test successful attach of a file reference in Turtle
     */
    @Ignore
    @Test
    public void testAttachFileReferenceBasicTurtle() throws Exception
    {
        final ClientResource fileRefAttachClientResource =
                new ClientResource(this.getUrl(PoddWebConstants.PATH_ATTACH_FILE_REF));
        
        Assert.fail("TODO: implement me");
    }
    
}
