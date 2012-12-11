/**
 * 
 */
package com.github.podd.api.test;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.github.podd.api.PoddOWLManager;

/**
 * Abstract test to verify that the PoddOWLManager API contract is followed by implementations.
 * 
 * TODO: add test cases for non-default cases (e.g. empty/null/invalid/non-matching values)
 * 
 * @author kutila
 * 
 */
public abstract class AbstractPoddOWLManagerTest
{
    
    protected PoddOWLManager testOWLManager;
    
    /**
     * @return A new instance of PoddOWLManager, for each call to this method
     */
    protected abstract PoddOWLManager getNewPoddOWLManagerInstance();
    
    /**
     * @return A new OWLReasonerFactory instance for use with the PoddOWLManager
     */
    protected abstract OWLReasonerFactory getNewOWLReasonerFactoryInstance();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testOWLManager = this.getNewPoddOWLManagerInstance();
        Assert.assertNotNull("Null implementation of test OWLManager", this.testOWLManager);
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testOWLManager = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Test
    public void testCacheSchemaOntology() throws Exception
    {
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
     * .
     * 
     */
    @Test
    public void testCreateReasoner() throws Exception
    {
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testGenerateInferredOntologyID() throws Exception
    {
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#getOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testGetOntology() throws Exception
    {
        
    }
    
    /**
     * Test method for {@link com.github.podd.api.PoddOWLManager#getReasonerProfile()} .
     * 
     */
    @Test
    public void testGetReasonerProfile() throws Exception
    {
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#inferStatements(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Test
    public void testInferStatements() throws Exception
    {
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
     * .
     * 
     */
    @Test
    public void testLoadOntology() throws Exception
    {
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#parseRDFStatements(org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)}
     * .
     * 
     */
    @Test
    public void testParseRDFStatements() throws Exception
    {
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#removeCache(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testRemoveCache() throws Exception
    {
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#setOWLOntologyManager(org.semanticweb.owlapi.model.OWLOntologyManager)}
     * .
     * 
     */
    @Test
    public void testSetOWLOntologyManager() throws Exception
    {
        final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
        
        this.testOWLManager.setOWLOntologyManager(manager);
        
        Assert.assertNotNull("OWLOntologyManager was not set", this.testOWLManager.getOWLOntologyManager());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#setReasonerFactory(org.semanticweb.owlapi.reasoner.OWLReasonerFactory)}
     * .
     * 
     */
    @Test
    public void testSetReasonerFactory() throws Exception
    {
        final OWLReasonerFactory reasonerFactory = this.getNewOWLReasonerFactoryInstance();
        Assert.assertNotNull("Null implementation of reasoner factory", reasonerFactory);
        
        this.testOWLManager.setReasonerFactory(reasonerFactory);
        
        Assert.assertNotNull("The reasoner factory was not set", this.testOWLManager.getReasonerFactory());
    }
    
    /**
     * Test method for {@link com.github.podd.api.PoddOWLManager#getOWLOntologyManager()} .
     * 
     */
    @Test
    public void testGetOWLOntologyManagerWithMockObject() throws Exception
    {
        Assert.assertNull("OWLOntologyManager should have been null", this.testOWLManager.getOWLOntologyManager());
        
        final OWLOntologyManager mockOWLOntologyManager = Mockito.mock(OWLOntologyManager.class);
        this.testOWLManager.setOWLOntologyManager(mockOWLOntologyManager);
        
        Assert.assertNotNull("OWLOntologyManager was not set", this.testOWLManager.getOWLOntologyManager());
        Assert.assertEquals("Not the expected mock OWLManager", mockOWLOntologyManager,
                this.testOWLManager.getOWLOntologyManager());
    }
    
    /**
     * Test method for {@link com.github.podd.api.PoddOWLManager#getReasonerFactory()} .
     * 
     */
    @Test
    public void testGetReasonerFactoryWithMockObject() throws Exception
    {
        Assert.assertNull("ReasonerFactory should have been null", this.testOWLManager.getReasonerFactory());
        
        final OWLReasonerFactory mockReasonerFactory = Mockito.mock(OWLReasonerFactory.class);
        
        this.testOWLManager.setReasonerFactory(mockReasonerFactory);
        
        Assert.assertNotNull("The reasoner factory was not set", this.testOWLManager.getReasonerFactory());
        Assert.assertEquals("Not the expected mock ReasonerFactory", mockReasonerFactory,
                this.testOWLManager.getReasonerFactory());
    }
}
