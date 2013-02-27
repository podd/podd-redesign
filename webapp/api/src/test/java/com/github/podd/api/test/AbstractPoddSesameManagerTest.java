/**
 * 
 */
package com.github.podd.api.test;

import info.aduna.iteration.Iterations;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddSesameManager;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
public abstract class AbstractPoddSesameManagerTest
{
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private PoddSesameManager testPoddSesameManager;
    
    private Repository testRepository;
    private RepositoryConnection testRepositoryConnection;
    
    private URI artifactGraph;
    
    private URI schemaGraph;
    
    public abstract PoddSesameManager getNewPoddSesameManagerInstance();
    
    /**
     * Helper method for testing
     * {@link com.github.podd.api.PoddSesameManager#isPublished(OWLOntologyID, RepositoryConnection)}
     * 
     */
    private boolean internalTestIsPublished(final String testResourcePath, final int expectedSize,
            final URI contextCumVersionIRI, final URI managementGraph) throws Exception
    {
        // prepare: load the ontology into the test repository
        final InputStream inputStream = this.getClass().getResourceAsStream(testResourcePath);
        this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, contextCumVersionIRI);
        Assert.assertEquals("Not the expected number of statements in Repository", expectedSize,
                this.testRepositoryConnection.size(contextCumVersionIRI));
        
        // prepare: build an OWLOntologyID
        final IRI ontologyIRI =
                this.testPoddSesameManager.getOntologyIRI(this.testRepositoryConnection, contextCumVersionIRI);
        final OWLOntologyID ontologyID = new OWLOntologyID(ontologyIRI.toOpenRDFURI(), contextCumVersionIRI);
        
        return this.testPoddSesameManager.isPublished(ontologyID, this.testRepositoryConnection, managementGraph);
    }
    
    /**
     * Helper method which populates a graph with artifact management triples.
     * 
     * @return The URI of the test artifact management graph
     * @throws Exception
     */
    private URI populateArtifactManagementGraph() throws Exception
    {
        final URI testOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/99-99/artifact:99");
        final URI testVersionURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/99-99/version:1");
        final URI testInferredURI =
                ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/99-99/version:1");
        
        this.testRepositoryConnection.add(testOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
        this.testRepositoryConnection.add(testInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.artifactGraph);
        this.testRepositoryConnection.add(testOntologyURI, OWL.VERSIONIRI, testVersionURI, this.artifactGraph);
        this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, testVersionURI,
                this.artifactGraph);
        this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                testInferredURI, this.artifactGraph);
        this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                testInferredURI, this.artifactGraph);
        
        return this.artifactGraph;
    }
    
    /**
     * Helper method which populates a graph with schema management triples.
     * 
     * @return The URI of the test schema management graph
     * @throws Exception
     */
    private URI populateSchemaManagementGraph() throws Exception
    {
        final URI pbBaseOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase");
        final URI pbVersionURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddBase/1");
        final URI pbInferredURI =
                ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
        
        final URI pScienceOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddScience");
        final URI pScienceVersionURI =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddScience/27");
        final URI pScienceInferredURI =
                ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddScience/43");
        
        final URI pPlantOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddPlant");
        final URI pPlantVersionURI =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/version/poddPlant/1");
        final URI pPlantInferredURI =
                ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/ns/version/poddPlant/1");
        
        // Podd-Base
        this.testRepositoryConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
        this.testRepositoryConnection.add(pbInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pbVersionURI,
                this.schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, pbVersionURI,
                this.schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                pbInferredURI, this.schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                pbInferredURI, this.schemaGraph);
        
        // Podd-Science
        this.testRepositoryConnection.add(pScienceOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
        this.testRepositoryConnection.add(pScienceInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pScienceVersionURI,
                this.schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION,
                pScienceVersionURI, this.schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, OWL.IMPORTS, pbVersionURI, this.schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                pScienceInferredURI, this.schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                pScienceInferredURI, this.schemaGraph);
        
        // Podd-Plant
        this.testRepositoryConnection.add(pPlantOntologyURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
        this.testRepositoryConnection.add(pPlantInferredURI, RDF.TYPE, OWL.ONTOLOGY, this.schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pPlantVersionURI,
                this.schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, pPlantVersionURI,
                this.schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, OWL.IMPORTS, pScienceVersionURI, this.schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, OWL.IMPORTS, pbVersionURI, this.schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                pPlantInferredURI, this.schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                pPlantInferredURI, this.schemaGraph);
        
        return this.schemaGraph;
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.artifactGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-mgt-graph:");
        this.schemaGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-mgt-graph:");
        
        this.testPoddSesameManager = this.getNewPoddSesameManagerInstance();
        Assert.assertNotNull("Null implementation of test OWLManager", this.testPoddSesameManager);
        
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
        
        this.testPoddSesameManager = null;
    }
    
    @Test
    public void testDeleteOntologiesSingleValid() throws Exception
    {
        // prepare: create artifact management graph
        final URI artifactGraph = this.populateArtifactManagementGraph();
        
        // invoke test method:
        final InferredOWLOntologyID inferredOntologyID =
                this.testPoddSesameManager.getCurrentArtifactVersion(
                        IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                        artifactGraph);
        
        // verify:
        Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
        Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                inferredOntologyID.getVersionIRI());
        Assert.assertEquals("Not the expected current inferred version",
                IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                inferredOntologyID.getInferredOntologyIRI());
        
        this.testPoddSesameManager.deleteOntologies(Arrays.asList(inferredOntologyID), this.testRepositoryConnection,
                artifactGraph);
        
        try
        {
            this.testPoddSesameManager.getCurrentArtifactVersion(inferredOntologyID.getOntologyIRI(),
                    this.testRepositoryConnection, artifactGraph);
            Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                    e.getMessage());
            Assert.assertEquals(inferredOntologyID.getOntologyIRI(), e.getOntologyID());
        }
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
     * .
     * 
     */
    @Test
    public void testGetCurrentArtifactVersionWithNullOntologyIRI() throws Exception
    {
        // prepare: create artifact management graph
        final URI artifactGraph = this.populateArtifactManagementGraph();
        
        try
        {
            this.testPoddSesameManager.getCurrentArtifactVersion(null, this.testRepositoryConnection, artifactGraph);
            Assert.fail("Should have thrown a RuntimeException");
        }
        catch(final RuntimeException e)
        {
            Assert.assertTrue("Not a NullPointerException as expected", e instanceof NullPointerException);
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
     * .
     * 
     */
    @Test
    public void testGetCurrentArtifactVersionWithOntologyIRI() throws Exception
    {
        // prepare: create artifact management graph
        final URI artifactGraph = this.populateArtifactManagementGraph();
        
        // invoke test method:
        final InferredOWLOntologyID inferredOntologyID =
                this.testPoddSesameManager.getCurrentArtifactVersion(
                        IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                        artifactGraph);
        
        // verify:
        Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
        Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                inferredOntologyID.getVersionIRI());
        Assert.assertEquals("Not the expected current inferred version",
                IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                inferredOntologyID.getInferredOntologyIRI());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
     * .
     * 
     */
    @Test
    public void testGetCurrentArtifactVersionWithUnmanagedOntologyIRI() throws Exception
    {
        // prepare: create artifact management graph
        final URI artifactGraph = this.populateArtifactManagementGraph();
        
        final IRI ontologyIRI = IRI.create("http://purl.org/podd/no-such-artifact:999");
        try
        {
            this.testPoddSesameManager.getCurrentArtifactVersion(ontologyIRI, this.testRepositoryConnection,
                    artifactGraph);
            Assert.fail("Should have thrown an UnmanagedArtifactIRIException");
        }
        catch(final UnmanagedArtifactIRIException e)
        {
            Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                    e.getMessage());
            Assert.assertEquals(ontologyIRI, e.getOntologyID());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getCurrentArtifactVersion(org.semanticweb.owlapi.model.IRI, org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)}
     * .
     * 
     */
    @Test
    public void testGetCurrentArtifactVersionWithVersionIRI() throws Exception
    {
        // prepare: create artifact management graph
        final URI artifactGraph = this.populateArtifactManagementGraph();
        
        // invoke test method:
        final InferredOWLOntologyID inferredOntologyID =
                this.testPoddSesameManager.getCurrentArtifactVersion(
                        IRI.create("http://purl.org/podd/99-99/version:1"), this.testRepositoryConnection,
                        artifactGraph);
        
        // verify:
        Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
        Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd/99-99/version:1"),
                inferredOntologyID.getVersionIRI());
        Assert.assertEquals("Not the expected current inferred version",
                IRI.create("urn:inferred:http://purl.org/podd/99-99/version:1"),
                inferredOntologyID.getInferredOntologyIRI());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     * 
     */
    @Test
    public void testGetCurrentSchemaVersionWithNullOntologyIRI() throws Exception
    {
        // prepare: create schema management graph
        final URI schemaGraph = this.populateSchemaManagementGraph();
        
        try
        {
            this.testPoddSesameManager.getCurrentSchemaVersion(null, this.testRepositoryConnection, schemaGraph);
            Assert.fail("Should have thrown a RuntimeException");
        }
        catch(final RuntimeException e)
        {
            Assert.assertTrue("Not a NullPointerException as expected", e instanceof NullPointerException);
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     * 
     */
    @Test
    public void testGetCurrentSchemaVersionWithOntologyIRI() throws Exception
    {
        // prepare: create schema management graph
        final URI schemaGraph = this.populateSchemaManagementGraph();
        
        // invoke test method:
        final InferredOWLOntologyID inferredOntologyID =
                this.testPoddSesameManager.getCurrentSchemaVersion(IRI.create("http://purl.org/podd/ns/poddBase"),
                        this.testRepositoryConnection, schemaGraph);
        
        // verify:
        Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
        Assert.assertEquals("Not the expected current version",
                IRI.create("http://purl.org/podd/ns/version/poddBase/1"), inferredOntologyID.getVersionIRI());
        Assert.assertEquals("Not the expected current inferred version",
                IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1"),
                inferredOntologyID.getInferredOntologyIRI());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     * 
     */
    @Test
    public void testGetCurrentSchemaVersionWithUnmanagedOntologyIRI() throws Exception
    {
        // prepare: create schema management graph
        final URI schemaGraph = this.populateSchemaManagementGraph();
        
        final IRI ontologyIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/999");
        try
        {
            this.testPoddSesameManager.getCurrentSchemaVersion(ontologyIRI, this.testRepositoryConnection, schemaGraph);
            Assert.fail("Should have thrown an UnmanagedSchemaIRIException");
        }
        catch(final UnmanagedSchemaIRIException e)
        {
            Assert.assertEquals("Not the expected exception", "This IRI does not refer to a managed ontology",
                    e.getMessage());
            Assert.assertEquals(ontologyIRI, e.getOntologyID());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getCurrentSchemaVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     * 
     */
    @Test
    public void testGetCurrentSchemaVersionWithVersionIRI() throws Exception
    {
        // prepare: create schema management graph
        final URI schemaGraph = this.populateSchemaManagementGraph();
        
        // invoke test method:
        final InferredOWLOntologyID inferredOntologyID =
                this.testPoddSesameManager.getCurrentSchemaVersion(
                        IRI.create("http://purl.org/podd/ns/version/poddScience/27"), this.testRepositoryConnection,
                        schemaGraph);
        
        // verify:
        Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
        Assert.assertEquals("Not the expected current version",
                IRI.create("http://purl.org/podd/ns/version/poddScience/27"), inferredOntologyID.getVersionIRI());
        Assert.assertEquals("Not the expected current inferred version",
                IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddScience/43"),
                inferredOntologyID.getInferredOntologyIRI());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getDirectImports(RepositoryConnection, URI)}.
     */
    @Test
    public void testGetDirectImports() throws Exception
    {
        final String resourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        
        final Repository testRepository = new SailRepository(new MemoryStore());
        testRepository.initialize();
        
        this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
        
        // invoke method under test:
        final Set<IRI> importedOntologyIRIs =
                this.testPoddSesameManager.getDirectImports(this.testRepositoryConnection, context);
        
        // verify:
        Assert.assertNotNull("No imports could be found", importedOntologyIRIs);
        Assert.assertEquals("Incorrect number of imports found", 4, importedOntologyIRIs.size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
     * .
     */
    @Test
    public void testGetOntologiesEmptyAllVersions() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final Collection<InferredOWLOntologyID> ontologies =
                this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
        
        Assert.assertNotNull(ontologies);
        Assert.assertTrue(ontologies.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
     * .
     */
    @Test
    public void testGetOntologiesEmptyOnlyCurrentVersions() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final Collection<InferredOWLOntologyID> ontologies =
                this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
        
        Assert.assertNotNull(ontologies);
        Assert.assertTrue(ontologies.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
     * .
     */
    @Test
    public void testGetOntologiesSingleAllVersions() throws Exception
    {
        final URI context = this.populateArtifactManagementGraph();
        
        final Collection<InferredOWLOntologyID> ontologies =
                this.testPoddSesameManager.getOntologies(false, this.testRepositoryConnection, context);
        
        Assert.assertNotNull(ontologies);
        Assert.assertEquals(1, ontologies.size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getOntologies(boolean, RepositoryConnection, URI)}
     * .
     */
    @Test
    public void testGetOntologiesSingleOnlyCurrentVersions() throws Exception
    {
        final URI context = this.populateArtifactManagementGraph();
        
        final Collection<InferredOWLOntologyID> ontologies =
                this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
        
        Assert.assertNotNull(ontologies);
        Assert.assertEquals(1, ontologies.size());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#getOntologyIRI(RepositoryConnection, URI)}.
     */
    @Test
    public void testGetOntologyIRI() throws Exception
    {
        final String resourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        
        this.testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
        
        // invoke method under test:
        final IRI ontologyIRI = this.testPoddSesameManager.getOntologyIRI(this.testRepositoryConnection, context);
        
        // verify:
        Assert.assertNotNull("Ontology IRI was null", ontologyIRI);
        Assert.assertEquals("Wrong Ontology IRI", "urn:temp:uuid:artifact:1", ontologyIRI.toString());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testIsPublishedWithEmptyOntology() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final OWLOntologyID emptyOntologyID = new OWLOntologyID();
        
        try
        {
            this.testPoddSesameManager.isPublished(emptyOntologyID, this.testRepositoryConnection, context);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testIsPublishedWithNullOntology() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        try
        {
            this.testPoddSesameManager.isPublished(null, this.testRepositoryConnection, context);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
     * . This test depends on imported PODD Base ontology being resolvable from
     * http://purl.org/podd/ns/poddBase.
     */
    @Test
    public void testIsPublishedWithPublishedArtifact() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final String testResourcePath = "/test/artifacts/basicProject-1-published.rdf";
        final URI versionUri = ValueFactoryImpl.getInstance().createURI("urn:temp:uuid:artifact:version:55");
        
        final boolean isPublished = this.internalTestIsPublished(testResourcePath, 23, versionUri, context);
        Assert.assertEquals("Did not identify artifact as Published", true, isPublished);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSesameManager#isPublished(org.semanticweb.owlapi.model.OWLOntologyID)}
     * . This test depends on imported PODD Base ontology being resolvable from
     * http://purl.org/podd/ns/poddBase.
     */
    @Test
    public void testIsPublishedWithUnPublishedArtifact() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final String testResourcePath = "/test/artifacts/basicProject-1.rdf";
        final URI versionUri = ValueFactoryImpl.getInstance().createURI("urn:temp:artifact:version:1");
        final boolean isPublished = this.internalTestIsPublished(testResourcePath, 23, versionUri, context);
        Assert.assertEquals("Did not identify artifact as Not Published", false, isPublished);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateCurrentManagedSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     */
    @Test
    public final void testUpdateCurrentManagedSchemaOntologyVersionWithoutUpdate() throws Exception
    {
        final IRI pOntologyIRI = IRI.create("http://purl.org/podd/ns/poddBase");
        final IRI pVersionIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/1");
        final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
        final InferredOWLOntologyID nextOntologyID =
                new InferredOWLOntologyID(pOntologyIRI, pVersionIRI, pInferredVersionIRI);
        
        // invoke method under test
        this.testPoddSesameManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        
        this.verifyManagementGraphContents(6, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRI);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateCurrentManagedSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     */
    @Test
    public final void testUpdateCurrentManagedSchemaOntologyVersionWithUpdate() throws Exception
    {
        final IRI pOntologyIRI = IRI.create("http://purl.org/podd/ns/poddBase");
        final IRI pVersionIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/1");
        final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
        final InferredOWLOntologyID nextOntologyID =
                new InferredOWLOntologyID(pOntologyIRI, pVersionIRI, pInferredVersionIRI);
        
        // first setting of schema versions in mgt graph
        this.testPoddSesameManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyID, false,
                this.testRepositoryConnection, this.schemaGraph);
        this.verifyManagementGraphContents(6, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRI);
        
        final IRI pVersionIRIUpdated = IRI.create("http://purl.org/podd/ns/version/poddBase/4");
        final IRI pInferredVersionIRIUpdated = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/5");
        final InferredOWLOntologyID nextOntologyIDUpdated =
                new InferredOWLOntologyID(pOntologyIRI, pVersionIRIUpdated, pInferredVersionIRIUpdated);
        
        // invoke with "updateCurrent" disallowed
        this.testPoddSesameManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyIDUpdated, false,
                this.testRepositoryConnection, this.schemaGraph);
        
        // verify only inferred ontology version is updated
        this.verifyManagementGraphContents(9, this.schemaGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRIUpdated);
        
        // invoke with "updateCurrent" allowed
        this.testPoddSesameManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyIDUpdated, true,
                this.testRepositoryConnection, this.schemaGraph);
        
        // verify both ontology current version and inferred ontology version haven been updated
        this.verifyManagementGraphContents(9, this.schemaGraph, pOntologyIRI, pVersionIRIUpdated,
                pInferredVersionIRIUpdated);
        
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     * 
     * Tests that when updating an artifact version, repository content for previous versions of the
     * artifact (both asserted and inferred statements) are deleted.
     * 
     */
    @Test
    public final void testUpdateManagedPoddArtifactVersionForDeletingPreviousVersionContent() throws Exception
    {
        // prepare: add entries in the artifact graph for a test artifact
        final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
        final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
        final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
        final InferredOWLOntologyID nextOntologyIDv1 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
        
        // prepare: add dummy statements in relevant contexts to represent test artifact
        final URI subject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
        this.testRepositoryConnection.add(subject, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pVersionIRIv1.toOpenRDFURI());
        
        final URI inferredSubject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
        this.testRepositoryConnection.add(inferredSubject, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pInferredVersionIRIv1.toOpenRDFURI());
        
        // verify: contexts populated for test artifact
        Assert.assertEquals("Asserted graph should have 1 statement", 1,
                this.testRepositoryConnection.size(pVersionIRIv1.toOpenRDFURI()));
        Assert.assertEquals("Inferred graph should have 1 statement", 1,
                this.testRepositoryConnection.size(pInferredVersionIRIv1.toOpenRDFURI()));
        
        // invoke method under test
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                this.testRepositoryConnection, this.artifactGraph);
        
        // verify: artifact management graph
        this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
        
        // prepare: version 2 of test artifact
        final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
        final InferredOWLOntologyID nextOntologyIDv2 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        // prepare: add dummy statements in relevant contexts for version 2 of test artifact
        final URI subject2 = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
        this.testRepositoryConnection.add(subject2, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pVersionIRIv2.toOpenRDFURI());
        
        final URI inferredSubject2 = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
        this.testRepositoryConnection.add(inferredSubject2, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pInferredVersionIRIv2.toOpenRDFURI());
        
        // verify: contexts populated for test artifact
        Assert.assertEquals("Asserted graph should have 1 statement", 1,
                this.testRepositoryConnection.size(pVersionIRIv2.toOpenRDFURI()));
        Assert.assertEquals("Inferred graph should have 1 statement", 1,
                this.testRepositoryConnection.size(pInferredVersionIRIv2.toOpenRDFURI()));
        
        // invoke method under test
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, true,
                this.testRepositoryConnection, this.artifactGraph);
        
        DebugUtils.printContexts(testRepositoryConnection);
        DebugUtils.printContents(testRepositoryConnection, this.artifactGraph);
        
        // verify:
        this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        DebugUtils.printContents(testRepositoryConnection, pInferredVersionIRIv1.toOpenRDFURI());
        
        Assert.assertEquals("Old asserted graph should be deleted", 0,
                this.testRepositoryConnection.size(pVersionIRIv1.toOpenRDFURI()));
        Assert.assertEquals("Old inferred graph should be deleted", 0,
                this.testRepositoryConnection.size(pInferredVersionIRIv1.toOpenRDFURI()));
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     * 
     * Details of an existing artifact are updated in the management graph.
     */
    @Test
    public final void testUpdateManagedPoddArtifactVersionWithExistingArtifact() throws Exception
    {
        // prepare: add entries in the artifact graph for a test artifact
        final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
        final IRI pVersion1IRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
        final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
        final InferredOWLOntologyID nextOntologyIDv1 =
                new InferredOWLOntologyID(pArtifactIRI, pVersion1IRIv1, pInferredVersionIRIv1);
        
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                this.testRepositoryConnection, this.artifactGraph);
        this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersion1IRIv1, pInferredVersionIRIv1);
        
        // prepare: update artifact version
        final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
        final InferredOWLOntologyID nextOntologyIDv2 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        // invoke method under test
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, true,
                this.testRepositoryConnection, this.artifactGraph);
        
        // verify: new version overwrites all references to the old version, and number of
        // statements stays the same
        this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     * 
     * Details of a new artifact are added to the management graph.
     */
    @Test
    public final void testUpdateManagedPoddArtifactVersionWithNewArtifact() throws Exception
    {
        // prepare: add entries in the artifact graph for a test artifact
        final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
        final IRI pVersionIRI = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
        final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
        final InferredOWLOntologyID nextOntologyID =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRI, pInferredVersionIRI);
        
        // invoke method under test
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyID, false,
                this.testRepositoryConnection, this.artifactGraph);
        
        // verify:
        this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRI, pInferredVersionIRI);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     * 
     * Details of an existing artifact are updated in the management graph, with "updateCurrent" =
     * false. The "current version" does not change for base/asserted ontology while the current
     * inferred version is updated.
     */
    @Test
    public final void testUpdateManagedPoddArtifactVersionWithoutUpdateCurrent() throws Exception
    {
        // prepare: add entries in the artifact graph for a test artifact
        final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
        final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
        final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
        final InferredOWLOntologyID nextOntologyIDv1 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
        
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                this.testRepositoryConnection, this.artifactGraph);
        this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
        
        // prepare: version 2
        final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
        final InferredOWLOntologyID nextOntologyIDv2 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        // invoke with "updateCurrent" disallowed
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, false,
                this.testRepositoryConnection, this.artifactGraph);
        
        // verify:
        this.verifyManagementGraphContents(11, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     */
    @Test
    public final void testUpdateManagedPoddArtifactVersionWithUpdate() throws Exception
    {
        // prepare: add entries in the artifact graph for a test artifact
        final IRI pArtifactIRI = IRI.create("http://purl.org/abc-def/artifact:1");
        final IRI pVersionIRIv1 = IRI.create("http://purl.org/abc-def/artifact:1:version:1");
        final IRI pInferredVersionIRIv1 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:1");
        final InferredOWLOntologyID nextOntologyIDv1 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
        
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv1, false,
                this.testRepositoryConnection, this.artifactGraph);
        this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
        
        // prepare: version 2
        final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
        final InferredOWLOntologyID nextOntologyIDv2 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        // invoke with "updateCurrent" disallowed
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv2, false,
                this.testRepositoryConnection, this.artifactGraph);
        
        DebugUtils.printContexts(testRepositoryConnection);
        DebugUtils.printContents(testRepositoryConnection, artifactGraph);
        
        // verify: version 2 is inserted, as verified by the extra statements, but the current
        // versions are not modified this time
        this.verifyManagementGraphContents(11, this.artifactGraph, pArtifactIRI, pVersionIRIv1, pInferredVersionIRIv1);
        
        // update to version 3
        final IRI pVersionIRIv3 = IRI.create("http://purl.org/abc-def/artifact:1:version:3");
        final IRI pInferredVersionIRIv3 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:3");
        final InferredOWLOntologyID nextOntologyIDv3 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv3, pInferredVersionIRIv3);
        
        // invoke with "updateCurrent" allowed
        this.testPoddSesameManager.updateManagedPoddArtifactVersion(nextOntologyIDv3, true,
                this.testRepositoryConnection, this.artifactGraph);
        
        this.verifyManagementGraphContents(7, this.artifactGraph, pArtifactIRI, pVersionIRIv3, pInferredVersionIRIv3);
    }
    
    /**
     * Helper method to verify the contents of a management graph
     * 
     * @param graphSize
     *            Expected size of the management graph
     * @param testGraph
     *            The management context/graph
     * @param ontologyIRI
     *            Ontology/artifact IRI to check against
     * @param expectedVersionIRI
     *            Expected current version IRI of the given ontology/artifact
     * @param expectedInferredVersionIRI
     *            Expected inferred version of the given ontology/artifact
     * @throws Exception
     */
    private void verifyManagementGraphContents(final int graphSize, final URI testGraph, final IRI ontologyIRI,
            final IRI expectedVersionIRI, final IRI expectedInferredVersionIRI) throws Exception
    {
        Assert.assertEquals("Graph not of expected size", graphSize, this.testRepositoryConnection.size(testGraph));
        
        Model stmtList =
                new LinkedHashModel(Iterations.asList(this.testRepositoryConnection.getStatements(null,
                        PoddRdfConstants.OMV_CURRENT_VERSION, null, false, testGraph)));
        Assert.assertEquals("Graph should have one OMV_CURRENT_VERSION statement", 1, stmtList.size());
        Assert.assertEquals("Wrong ontology IRI", ontologyIRI.toOpenRDFURI(), stmtList.subjects().iterator().next());
        Assert.assertEquals("Wrong version IRI", expectedVersionIRI.toOpenRDFURI(), stmtList.objectURI());
        
        Model inferredVersionStatementList =
                new LinkedHashModel(Iterations.asList(this.testRepositoryConnection.getStatements(null,
                        PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null, false, testGraph)));
        Assert.assertEquals("Graph should have one CURRENT_INFERRED_VERSION statement", 1,
                inferredVersionStatementList.size());
        Assert.assertEquals("Wrong ontology IRI", ontologyIRI.toOpenRDFURI(), inferredVersionStatementList.subjects()
                .iterator().next());
        Assert.assertEquals("Wrong version IRI", expectedInferredVersionIRI.toOpenRDFURI(),
                inferredVersionStatementList.objectURI());
    }
}
