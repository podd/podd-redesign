/**
 * 
 */
package org.github.podd.client.api.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
    protected abstract String getPoddServerUrl();
    
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
    @Test
    public final void testAppendArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#attachFileReference(org.semanticweb.owlapi.model.IRI, org.semanticweb.owlapi.model.IRI, java.lang.String, java.lang.String, java.lang.String)}
     * .
     */
    @Test
    public final void testAttachFileReference()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#deleteArtifact(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Test
    public final void testDeleteArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#getPoddServerUrl()}.
     */
    @Test
    public final void testGetPoddServerUrlNull()
    {
        // Server URL is null by default until setPoddServerUrl is called
        Assert.assertNull(this.testClient.getPoddServerUrl());
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#listFileReferenceRepositories()}
     * .
     */
    @Test
    public final void testListFileReferenceRepositories()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#listPublishedArtifacts()}.
     */
    @Test
    public final void testListPublishedArtifacts()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#listUnpublishedArtifacts()}.
     */
    @Test
    public final void testListUnpublishedArtifacts()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#login(java.lang.String, String)}
     * .
     */
    @Test
    public final void testLogin()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for {@link com.github.podd.client.api.PoddClient#logout()}.
     */
    @Test
    public final void testLogout()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#publishArtifact(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Test
    public final void testPublishArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#setPoddServerUrl(java.lang.String)}.
     */
    @Test
    public final void testSetPoddServerUrl()
    {
        Assert.assertNull(this.testClient.getPoddServerUrl());
        
        this.testClient.setPoddServerUrl(this.getPoddServerUrl());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#unpublishArtifact(org.semanticweb.owlapi.model.IRI)}
     * .
     */
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
    @Test
    public final void testUpdateArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.client.api.PoddClient#uploadNewArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadNewArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
}
