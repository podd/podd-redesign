/**
 * 
 */
package org.github.podd.client.api.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.client.api.PoddClient;

/**
 * Abstract tests for {@link PoddClient}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public abstract class AbstractPoddClientTest
{
    private static final String TEST_ADMIN_PASSWORD = "testAdminPassword";
    private static final String TEST_ADMIN_USER = "testAdminUser";
    private PoddClient testClient;
    
    /**
     * Implementing test classes must return a new instance of {@link PoddClient} for each call to
     * this method.
     * 
     * @return A new instance of {@link PoddClient}.
     */
    protected abstract PoddClient getNewPoddClientInstance();
    
    /**
     * Returns the URL of a running PODD Server to test the client against.
     * 
     * @return The URL of the PODD Server to test using.
     */
    protected abstract String getTestPoddServerUrl();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testClient = this.getNewPoddClientInstance();
        Assert.assertNotNull("PODD Client implementation was null", this.testClient);
        
        this.testClient.setPoddServerUrl(this.getTestPoddServerUrl());
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testClient = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#appendArtifact(OWLOntologyID, InputStream, RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testAppendArtifact() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#attachFileReference(OWLOntologyID, org.semanticweb.owlapi.model.IRI, String, String, String)}
     * .
     */
    @Ignore
    @Test
    public final void testAttachFileReference() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#deleteArtifact(OWLOntologyID)} .
     */
    @Ignore
    @Test
    public final void testDeleteArtifact() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#downloadArtifact(OWLOntologyID, java.io.OutputStream, RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadArtifactCurrentVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#downloadArtifact(OWLOntologyID, java.io.OutputStream, RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadArtifactDummyVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#downloadArtifact(OWLOntologyID, java.io.OutputStream, RDFFormat)}
     * .
     */
    @Test
    public final void testDownloadArtifactNoVersion() throws Exception
    {
        this.testClient.login(AbstractPoddClientTest.TEST_ADMIN_USER, AbstractPoddClientTest.TEST_ADMIN_PASSWORD);
        
        final InputStream input = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        Assert.assertNotNull("Test resource missing", input);
        
        final OWLOntologyID newArtifact = this.testClient.uploadNewArtifact(input, RDFFormat.RDFXML);
        Assert.assertNotNull(newArtifact);
        Assert.assertNotNull(newArtifact.getOntologyIRI());
        Assert.assertNotNull(newArtifact.getVersionIRI());
        
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(8096);
        
        this.testClient.downloadArtifact(new OWLOntologyID(newArtifact.getOntologyIRI()), outputStream,
                RDFFormat.RDFJSON);
        
        final Model model = new LinkedHashModel();
        
        final RDFParser parser = Rio.createParser(RDFFormat.RDFJSON);
        parser.setRDFHandler(new StatementCollector(model));
        parser.parse(new ByteArrayInputStream(outputStream.toByteArray()), "");
        
        Assert.assertEquals(31, model.size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#downloadArtifact(OWLOntologyID, java.io.OutputStream, RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadArtifactOldVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#getPoddServerUrl()}.
     */
    @Test
    public final void testGetPoddServerUrlNull() throws Exception
    {
        Assert.assertNotNull(this.testClient.getPoddServerUrl());
        
        this.testClient.setPoddServerUrl(null);
        
        Assert.assertNull(this.testClient.getPoddServerUrl());
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#listFileReferenceRepositories()}
     * .
     */
    @Ignore
    @Test
    public final void testListFileReferenceRepositories() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#listPublishedArtifacts()}.
     */
    @Ignore
    @Test
    public final void testListPublishedArtifacts() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#listUnpublishedArtifacts()}.
     */
    @Ignore
    @Test
    public final void testListUnpublishedArtifacts() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#login(java.lang.String, String)}
     * .
     */
    @Test
    public final void testLogin() throws Exception
    {
        Assert.assertFalse(this.testClient.isLoggedIn());
        
        Assert.assertTrue("Client was not successfully logged in",
                this.testClient.login("testAdminUser", "testAdminPassword"));
        
        Assert.assertTrue(this.testClient.isLoggedIn());
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#logout()}.
     */
    @Test
    public final void testLogout() throws Exception
    {
        this.testClient.setPoddServerUrl(this.getTestPoddServerUrl());
        
        Assert.assertFalse(this.testClient.isLoggedIn());
        
        Assert.assertTrue("Client was not successfully logged in",
                this.testClient.login("testAdminUser", "testAdminPassword"));
        
        Assert.assertTrue(this.testClient.isLoggedIn());
        
        Assert.assertTrue("Client was not successfully logged out", this.testClient.logout());
        
        Assert.assertFalse(this.testClient.isLoggedIn());
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#publishArtifact(OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testPublishArtifact() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#setPoddServerUrl(java.lang.String)}.
     */
    @Test
    public final void testSetPoddServerUrl() throws Exception
    {
        Assert.assertNotNull(this.testClient.getPoddServerUrl());
        
        this.testClient.setPoddServerUrl(null);
        
        Assert.assertNull(this.testClient.getPoddServerUrl());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#unpublishArtifact(OWLOntologyID)} .
     */
    @Ignore
    @Test
    public final void testUnpublishArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#updateArtifact(OWLOntologyID, InputStream, RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUpdateArtifact() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#uploadNewArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadNewArtifact() throws Exception
    {
        this.testClient.login(AbstractPoddClientTest.TEST_ADMIN_USER, AbstractPoddClientTest.TEST_ADMIN_PASSWORD);
        
        final InputStream input = this.getClass().getResourceAsStream("/test/artifacts/basicProject-1.rdf");
        Assert.assertNotNull("Test resource missing", input);
        
        final OWLOntologyID newArtifact = this.testClient.uploadNewArtifact(input, RDFFormat.RDFXML);
        Assert.assertNotNull(newArtifact);
        Assert.assertNotNull(newArtifact.getOntologyIRI());
        Assert.assertNotNull(newArtifact.getVersionIRI());
    }
    
}
