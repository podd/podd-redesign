/*
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
package com.github.podd.api.purl.test;

import java.io.InputStream;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
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
import com.github.podd.api.test.TestConstants;

/**
 * Abstract class to test PoddPurlManager.
 * 
 * @author kutila
 */
public abstract class AbstractPoddPurlManagerTest
{
    
    protected static final String TEMP_URI_PREFIX = "urn:temp";
    
    protected PoddPurlManager testPurlManager;
    
    private PoddPurlProcessorFactoryRegistry testRegistry;
    
    private Repository testRepository;
    
    protected RepositoryConnection testRepositoryConnection;
    
    /**
     * @return A new PoddPurlManager instance for use by this test
     */
    public abstract PoddPurlManager getNewPoddPurlManager();
    
    /**
     * @return A new PurlProcessorFactory Registry for use by this test
     */
    public abstract PoddPurlProcessorFactoryRegistry getNewPoddPurlProcessorFactoryRegistry();
    
    /**
     * Helper method loads RDF statements from a test resource into the test Repository.
     * 
     * @return The context into which the statements are loaded
     * @throws Exception
     */
    protected URI loadTestResources() throws Exception
    {
        final String resourcePath = TestConstants.TEST_ARTIFACT_BASIC_1_INTERNAL_OBJECT;
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
        
        return context;
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
    @Test
    public void testConvertTemporaryUris() throws Exception
    {
        final URI context = this.loadTestResources();
        final long repoSize = this.testRepositoryConnection.size();
        
        final Set<PoddPurlReference> purlSet =
                this.testPurlManager.extractPurlReferences(this.testRepositoryConnection, context);
        
        this.testPurlManager.convertTemporaryUris(purlSet, this.testRepositoryConnection, context);
        
        // verify temporary URIs no longer exist in the Repository
        final RepositoryResult<Statement> repoContents =
                this.testRepositoryConnection.getStatements(null, null, null, false, context);
        while(repoContents.hasNext())
        {
            final Statement statement = repoContents.next();
            Assert.assertFalse("Temporary URI exists in Subject",
                    AbstractPoddPurlManagerTest.TEMP_URI_PREFIX.contains(statement.getSubject().stringValue()));
            Assert.assertFalse("Temporary URI exists in Object",
                    AbstractPoddPurlManagerTest.TEMP_URI_PREFIX.contains(statement.getObject().stringValue()));
            // System.out.println(statement.toString());
        }
        
        // verify generated Purls exist in the Repository
        for(final PoddPurlReference purl : purlSet)
        {
            // check each Purl is present in the updated RDF statements as a subject or object
            final boolean purlExistsAsSubject =
                    this.testRepositoryConnection.getStatements(purl.getPurlURI(), null, null, false, context)
                            .hasNext();
            final boolean purlExistsAsObject =
                    this.testRepositoryConnection.getStatements(null, null, purl.getPurlURI(), false, context)
                            .hasNext();
            Assert.assertTrue("Purl not found in updated RDF statements", purlExistsAsSubject || purlExistsAsObject);
        }
        
        // NOTE: It is possible that future Purl Generators may add new statements, thereby altering
        // repository size.
        // In such situations, this assertion would fail and have to be adjusted.
        Assert.assertEquals("Repository size should not have changed", repoSize, this.testRepositoryConnection.size());
    }
    
    @Test
    public void testConvertTemporaryUrisWithEmptyPurlSet() throws Exception
    {
        final URI context = this.loadTestResources();
        final long repoSize = this.testRepositoryConnection.size();
        final Set<PoddPurlReference> emptyPurlSet = new java.util.HashSet<PoddPurlReference>();
        
        this.testPurlManager.convertTemporaryUris(emptyPurlSet, this.testRepositoryConnection, context);
        
        // no exceptions are expected, but Repository would remain unchanged
        Assert.assertEquals("Repository Size should not have changed", repoSize, this.testRepositoryConnection.size());
    }
    
    /**
     * Tests calling convertTemporaryUris() with a non-empty PurlSet on an empty Repository.
     * 
     * NOTE: If this test fails at some stage due to a PurlManager implementation deciding to throw
     * an Exception instead of silently failing, this would have to be moved into the concrete tests
     * for PoddPurlManagerImpl.java.
     * 
     * @throws Exception
     */
    @Test
    public void testConvertTemporaryUrisWithEmptyRepositoryConnection() throws Exception
    {
        final URI context = this.loadTestResources();
        final Set<PoddPurlReference> purlSet =
                this.testPurlManager.extractPurlReferences(this.testRepositoryConnection, context);
        
        // create an empty repository and to call convertTemporaryUris() on it
        final Repository emptyRepository = new SailRepository(new MemoryStore());
        emptyRepository.initialize();
        final RepositoryConnection emptyRepositoryConnection = emptyRepository.getConnection();
        emptyRepositoryConnection.begin();
        
        Assert.assertEquals("New Repository should have been empty", 0, emptyRepositoryConnection.size());
        
        try
        {
            // convertTemporaryUris() will have no effect as our Repository is empty
            this.testPurlManager.convertTemporaryUris(purlSet, emptyRepositoryConnection, context);
            
            Assert.assertEquals("Repository should still be empty", 0, emptyRepositoryConnection.size());
        }
        finally
        {
            emptyRepositoryConnection.rollback();
            emptyRepositoryConnection.close();
            emptyRepository.shutDown();
        }
    }
    
    @Test
    public void testConvertTemporaryUrisWithNullPurlSet() throws Exception
    {
        final URI context = this.loadTestResources();
        try
        {
            this.testPurlManager.convertTemporaryUris(null, this.testRepositoryConnection, context);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            // this expected Exception is thrown without a message
            Assert.assertEquals("A NULL message was expected", null, e.getMessage());
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
        final URI context = this.loadTestResources();
        
        final Set<PoddPurlReference> purlSet =
                this.testPurlManager.extractPurlReferences(this.testRepositoryConnection, context);
        
        Assert.assertNotNull("Extracted Purl references were null", purlSet);
        Assert.assertFalse("Extracted Purl references were empty", purlSet.isEmpty());
        Assert.assertEquals("Incorrect number of Purl references extracted", 3, purlSet.size());
        
        for(final PoddPurlReference purl : purlSet)
        {
            Assert.assertNotNull("Purl has null temporary URI", purl.getTemporaryURI());
            Assert.assertNotNull("Purl has null permanent URI", purl.getPurlURI());
            
            Assert.assertFalse("Purl and Temporary URI were same", purl.getPurlURI().equals(purl.getTemporaryURI()));
            
            // check temporary URI is present in the original RDF statements as a subject or object
            final boolean tempUriExistsAsSubject =
                    this.testRepositoryConnection.getStatements(purl.getTemporaryURI(), null, null, false, context)
                            .hasNext();
            final boolean tempUriExistsAsObject =
                    this.testRepositoryConnection.getStatements(null, null, purl.getTemporaryURI(), false, context)
                            .hasNext();
            Assert.assertTrue("Temporary URI not found in original RDF statements", tempUriExistsAsSubject
                    || tempUriExistsAsObject);
        }
    }
    
    /**
     * Tests extracting Purl references from an empty repository returns an empty PurlSet.
     * 
     * @throws Exception
     */
    @Test
    public void testExtractPurlReferencesFromEmptyRepository() throws Exception
    {
        final Set<PoddPurlReference> purlSet =
                this.testPurlManager.extractPurlReferences(this.testRepositoryConnection);
        
        Assert.assertNotNull("Extracted Purl references were null", purlSet);
        Assert.assertTrue("Extracted Purl references should be empty", purlSet.isEmpty());
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
