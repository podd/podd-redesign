/**
 * 
 */
package com.github.podd.api.purl.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.api.purl.PoddPurlReference;

/**
 * Abstract class to test PoddPurlManager.
 * 
 * @author kutila
 */
public abstract class AbstractPoddPurlManagerTest
{
    
    protected static final String TEMP_URI_PREFIX = "urn:temp";
    
    private PoddPurlManager testPurlManager;
    
    private PoddPurlProcessorFactoryRegistry testRegistry;
    
    private Repository testRepository;
    
    private RepositoryConnection testRepositoryConnection;
    
    /**
     * @return A new PoddPurlManager instance for use by this test
     */
    public abstract PoddPurlManager getNewPoddPurlManager();
    
    /**
     * @return A new PurlProcessorFactory Registry for use by this test
     */
    public abstract PoddPurlProcessorFactoryRegistry getNewPoddPurlProcessorFactoryRegistry();
    
    /**
     * Helper method to read a classpath resource into a String.
     * 
     * @param resourcePath
     * @return
     * @throws IOException
     */
    private String getResourceAsString(final String resourcePath) throws IOException
    {
        final StringBuilder b = new StringBuilder();
        final BufferedReader bReader =
                new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(resourcePath),
                        StandardCharsets.UTF_8));
        for(int c = bReader.read(); c != -1; c = bReader.read())
        {
            b.append((char)c);
        }
        final String rdfResourceString = b.toString();
        return rdfResourceString;
    }
    
    /**
     * Helper method to load RDF statements from the specified resource to the given context in the
     * test Repository.
     * 
     * @param resourcePath
     *            Path to resource from which statements are to be loaded
     * @throws Exception
     */
    private void loadResourceToRepository(final String resourcePath, final URI context) throws Exception
    {
        if(resourcePath != null)
        {
            final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
            Assert.assertNotNull("Could not find resource", inputStream);
            this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
        }
    }
    
    @Before
    public void setUp() throws Exception
    {
        this.testRegistry = this.getNewPoddPurlProcessorFactoryRegistry();
        Assert.assertNotNull("Null implementation of test Registry", this.testRegistry);
        
        this.testPurlManager = this.getNewPoddPurlManager();
        Assert.assertNotNull("Null implementation of test PurlManager", this.testPurlManager);
        
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        this.testRepositoryConnection = this.testRepository.getConnection();
        this.testRepositoryConnection.begin(); // Transaction per each test
        
        this.testPurlManager.setPurlProcessorFactoryRegistry(this.testRegistry);
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.testRegistry.clear();
        this.testRepositoryConnection.rollback();
        this.testRepositoryConnection.close();
        this.testRepository.shutDown();
        
        this.testPurlManager = null;
    }
    
    /**
     * Tests that a PurlManager is able to replace temporary URIs in a given context of a Repository
     * with previously generated Purls.
     * 
     * This test makes use of PurlManager.extractPurlReferences() to generate new Purls.
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public void testConvertTemporaryUris() throws Exception
    {
        final String resourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        this.loadResourceToRepository(resourcePath, context);
        
        final Set<PoddPurlReference> purlSet =
                this.testPurlManager.extractPurlReferences(this.testRepositoryConnection, context);
        
        this.testPurlManager.convertTemporaryUris(purlSet, this.testRepositoryConnection, context);
        
        final RepositoryResult<Statement> repoContents =
                this.testRepositoryConnection.getStatements(null, null, null, false, context);
        while(repoContents.hasNext())
        {
            final Statement statement = repoContents.next();
            Assert.assertFalse("Temporary URI exists in Subject",
                    AbstractPoddPurlManagerTest.TEMP_URI_PREFIX.contains(statement.getSubject().stringValue()));
            Assert.assertFalse("Temporary URI exists in Object",
                    AbstractPoddPurlManagerTest.TEMP_URI_PREFIX.contains(statement.getObject().stringValue()));
        }
    }
    
    /**
     * Tests that a PurlManager is able to correctly identify temporary URIs in a given context of a
     * Repository and generate Purls for them.
     * 
     * @throws Exception
     */
    @Test
    public void testExtractPurlReferences() throws Exception
    {
        final String resourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        this.loadResourceToRepository(resourcePath, context);
        
        final Set<PoddPurlReference> purlSet =
                this.testPurlManager.extractPurlReferences(this.testRepositoryConnection, context);
        
        Assert.assertNotNull("Extracted Purl references were null", purlSet);
        Assert.assertFalse("Extracted Purl references were empty", purlSet.isEmpty());
        Assert.assertEquals("Incorrect number of Purl references extracted", 3, purlSet.size());
        
        // get a String representation of the RDF resource for verification purposes
        final String rdfResourceString = this.getResourceAsString(resourcePath);
        
        for(final PoddPurlReference purl : purlSet)
        {
            Assert.assertNotNull("Purl has null temporary URI", purl.getTemporaryURI());
            Assert.assertNotNull("Purl has null permanent URI", purl.getPurlURI());
            
            Assert.assertFalse("Purl and Temporary URI were same", purl.getPurlURI().equals(purl.getTemporaryURI()));
            
            // comparing separately in case namespace prefix is used
            Assert.assertTrue(rdfResourceString.contains(purl.getTemporaryURI().getNamespace()));
            Assert.assertTrue(rdfResourceString.contains(purl.getTemporaryURI().getLocalName()));
            
            // further Purl verification requires implementation awareness
        }
    }
    
    @Test
    public void testGetPurlProcessorFactoryRegistry() throws Exception
    {
        Assert.assertNotNull("getRegistry() returned null", this.testPurlManager.getPurlProcessorFactoryRegistry());
    }
    
    @Test
    public void testSetPurlProcessorFactoryRegistry() throws Exception
    {
        // first set the Registry to Null and verify it
        this.testPurlManager.setPurlProcessorFactoryRegistry(null);
        Assert.assertNull("Registry was not set to null", this.testPurlManager.getPurlProcessorFactoryRegistry());
        
        // set the Registry
        this.testPurlManager.setPurlProcessorFactoryRegistry(this.testRegistry);
        
        Assert.assertNotNull("getRegistry() returned null ", this.testPurlManager.getPurlProcessorFactoryRegistry());
    }
    
}
