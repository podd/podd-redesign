/**
 * 
 */
package org.github.podd.client.api.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.github.podd.client.api.PoddClient;

/**
 * Abstract tests for {@link PoddClient}.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public abstract class AbstractPoddClientTest
{
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
     * {@link com.github.podd.client.api.PoddClient#appendArtifact(org.semanticweb.owlapi.model.IRI, java.io.InputStream, org.openrdf.rio.RDFFormat)}
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
     * {@link com.github.podd.client.api.PoddClient#attachFileReference(org.semanticweb.owlapi.model.IRI, org.semanticweb.owlapi.model.IRI, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Ignore
    @Test
    public final void testAttachFileReference() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#deleteArtifact(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testDeleteArtifact() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#getPoddServerUrl()}.
     */
    @Test
    public final void testGetPoddServerUrlNull() throws Exception
    {
        // Server URL is null by default until setPoddServerUrl is called
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
        this.testClient.setPoddServerUrl(this.getTestPoddServerUrl());
        
        Assert.assertFalse(this.testClient.isLoggedIn());
        
        Assert.assertTrue("Client was not successfully logged in",
                this.testClient.login("testAdminUser", "testAdminPassword"));
        
        Assert.assertTrue(this.testClient.isLoggedIn());
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#logout()}.
     */
    @Ignore
    @Test
    public final void testLogout() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#publishArtifact(org.semanticweb.owlapi.model.IRI)}
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
        Assert.assertNull(this.testClient.getPoddServerUrl());
        
        this.testClient.setPoddServerUrl(this.getTestPoddServerUrl());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#unpublishArtifact(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testUnpublishArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#updateArtifact(org.semanticweb.owlapi.model.IRI, java.io.InputStream, org.openrdf.rio.RDFFormat)}
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
    @Ignore
    @Test
    public final void testUploadNewArtifact() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
}
