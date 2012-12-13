/**
 * 
 */
package com.github.podd.api.test;

import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;

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
    
    private Repository testRepository;
    
    protected RepositoryConnection testRepositoryConnection;
    
    private String poddBaseResourcePath = "/ontologies/poddBase.owl";
    
    /**
     * @return A new OWLReasonerFactory instance for use with the PoddOWLManager
     */
    protected abstract OWLReasonerFactory getNewOWLReasonerFactoryInstance();
    
    /**
     * @return A new instance of PoddOWLManager, for each call to this method
     */
    protected abstract PoddOWLManager getNewPoddOWLManagerInstance();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testOWLManager = this.getNewPoddOWLManagerInstance();
        Assert.assertNotNull("Null implementation of test OWLManager", this.testOWLManager);
        
        // set an OWLOntologyManager for this PoddOWLManager
        final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
        this.testOWLManager.setOWLOntologyManager(manager);
        
        // create a memory Repository for tests
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        this.testRepositoryConnection = this.testRepository.getConnection();
        this.testRepositoryConnection.begin();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testRepositoryConnection.rollback();
        this.testRepositoryConnection.close();
        this.testRepository.shutDown();
        
        this.testOWLManager = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testCacheSchemaOntology() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testCreateReasoner() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testGenerateInferredOntologyID() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#getCurrentVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testGetCurrentVersion() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#getOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testGetOntology() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for {@link com.github.podd.api.PoddOWLManager#getOWLOntologyManager()} .
     * 
     */
    @Test
    public void testGetOWLOntologyManagerWithMockObject() throws Exception
    {
        this.testOWLManager.setOWLOntologyManager(null);
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
    
    /**
     * Test method for {@link com.github.podd.api.PoddOWLManager#getReasonerProfile()} .
     * 
     */
    @Ignore
    @Test
    public void testGetReasonerProfile() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#getVersions(org.semanticweb.owlapi.model.IRI)} .
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public void testGetVersion() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#inferStatements(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testInferStatements() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#isPublished(org.semanticweb.owlapi.model.IRI)} .
     * 
     */
    @Ignore
    @Test
    public void testIsPublishedIRI() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testIsPublishedOWLOntologyID() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
     * .
     * 
     */
    @Test
    public void testLoadOntologyFromRioMemoryTripleSource() throws Exception
    {
        // prepare: load an ontology into a RioMemoryTripleSource via the test repository
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:context:test");
        
        final InputStream inputStream = this.getClass().getResourceAsStream(this.poddBaseResourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        
        this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
        final List<Statement> statements =
                this.testRepositoryConnection.getStatements(null, null, null, false, context).asList();
        Assert.assertEquals("Not the expected number of statements in Repository", 278, statements.size());
        
        final RioMemoryTripleSource owlSource = new RioMemoryTripleSource(statements.iterator());
        
        final OWLOntology loadedOntology = this.testOWLManager.loadOntology(owlSource);
        
        // verify:
        Assert.assertNotNull(loadedOntology);
        Assert.assertEquals("<http://purl.org/podd/ns/poddBase>", loadedOntology.getOntologyID().getOntologyIRI()
                .toQuotedString());
        Assert.assertEquals("<http://purl.org/podd/ns/version/poddBase/1>", loadedOntology.getOntologyID()
                .getVersionIRI().toQuotedString());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
     * .
     * 
     */
    @Test
    public void testLoadOntologyFromOWLOntologyDocumentSource() throws Exception
    {
        // prepare: load an ontology into a StreamDocumentSource
        final InputStream inputStream = this.getClass().getResourceAsStream(this.poddBaseResourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        
        final OWLOntologyDocumentSource owlSource =
                new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                        RDFFormat.RDFXML.getDefaultMIMEType()));
        
        final OWLOntology loadedOntology = this.testOWLManager.loadOntology(owlSource);
        
        // verify:
        Assert.assertNotNull(loadedOntology);
        Assert.assertEquals("<http://purl.org/podd/ns/poddBase>", loadedOntology.getOntologyID().getOntologyIRI()
                .toQuotedString());
        Assert.assertEquals("<http://purl.org/podd/ns/version/poddBase/1>", loadedOntology.getOntologyID()
                .getVersionIRI().toQuotedString());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#parseRDFStatements(org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testParseRDFStatements() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#removeCache(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testRemoveCache() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#setCurrentVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Ignore
    @Test
    public void testSetCurrentVersion() throws Exception
    {
        Assert.fail("TODO: Implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#setOWLOntologyManager(org.semanticweb.owlapi.model.OWLOntologyManager)}
     * .
     * 
     */
    @Test
    public void testSetOWLOntologyManagerWithNull() throws Exception
    {
        this.testOWLManager.setOWLOntologyManager(null);
        Assert.assertNull("OWLOntologyManager could not be set to NULL", this.testOWLManager.getOWLOntologyManager());
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
        // set null to forget the manager being set in setUp()
        this.testOWLManager.setOWLOntologyManager(null);
        Assert.assertNull("OWLOntologyManager could not be set to NULL", this.testOWLManager.getOWLOntologyManager());
        
        final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
        
        this.testOWLManager.setOWLOntologyManager(manager);
        
        Assert.assertNotNull("OWLOntologyManager was not set", this.testOWLManager.getOWLOntologyManager());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#setPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     * @throws Exception
     */
    @Ignore
    @Test
    public void testSetPublished() throws Exception
    {
        Assert.fail("TODO: Implement me");
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
}
