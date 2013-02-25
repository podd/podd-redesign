/**
 * 
 */
package com.github.podd.api.test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
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
    
    public abstract PoddSesameManager getNewPoddSesameManagerInstance();
    
    /**
     * Helper method for testing
     * {@link com.github.podd.api.PoddSesameManager#isPublished(OWLOntologyID, RepositoryConnection)}
     * 
     */
    private boolean internalTestIsPublished(final String testResourcePath, final int expectedSize,
            final URI contextCumVersionIRI) throws Exception
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
        
        return this.testPoddSesameManager.isPublished(ontologyID, this.testRepositoryConnection);
    }
    
    /**
     * Helper method which populates a graph with artifact management triples.
     * 
     * @return The URI of the test artifact management graph
     * @throws Exception
     */
    private URI populateArtifactManagementGraph() throws Exception
    {
        final URI artifactGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-mgt-graph:");
        
        final URI testOntologyURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/99-99/artifact:99");
        final URI testVersionURI = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd//99-99/version:1");
        final URI testInferredURI =
                ValueFactoryImpl.getInstance().createURI("urn:inferred:http://purl.org/podd/99-99/version:1");
        
        this.testRepositoryConnection.add(testOntologyURI, RDF.TYPE, OWL.ONTOLOGY, artifactGraph);
        this.testRepositoryConnection.add(testInferredURI, RDF.TYPE, OWL.ONTOLOGY, artifactGraph);
        this.testRepositoryConnection.add(testOntologyURI, OWL.VERSIONIRI, testVersionURI, artifactGraph);
        this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, testVersionURI,
                artifactGraph);
        this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                testInferredURI, artifactGraph);
        this.testRepositoryConnection.add(testOntologyURI, PoddRdfConstants.PODD_BASE_INFERRED_VERSION,
                testInferredURI, artifactGraph);
        
        return artifactGraph;
    }
    
    /**
     * Helper method which populates a graph with schema management triples.
     * 
     * @return The URI of the test schema management graph
     * @throws Exception
     */
    private URI populateSchemaManagementGraph() throws Exception
    {
        final URI schemaGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-mgt-graph:");
        
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
        this.testRepositoryConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pbVersionURI,
                schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, pbVersionURI,
                schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                pbInferredURI, schemaGraph);
        
        // Podd-Science
        this.testRepositoryConnection.add(pScienceOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pScienceVersionURI,
                schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION,
                pScienceVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pScienceOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                pScienceInferredURI, schemaGraph);
        
        // Podd-Plant
        this.testRepositoryConnection.add(pPlantOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.OWL_VERSION_IRI, pPlantVersionURI,
                schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.OMV_CURRENT_VERSION, pPlantVersionURI,
                schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, OWL.IMPORTS, pScienceVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pPlantOntologyURI, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION,
                pPlantInferredURI, schemaGraph);
        
        return schemaGraph;
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
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
                        IRI.create("http://purl.org/podd//99-99/version:1"), this.testRepositoryConnection,
                        artifactGraph);
        
        // verify:
        Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
        Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd//99-99/version:1"),
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
                        IRI.create("http://purl.org/podd//99-99/version:1"), this.testRepositoryConnection,
                        artifactGraph);
        
        // verify:
        Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
        Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd//99-99/version:1"),
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
                        IRI.create("http://purl.org/podd//99-99/version:1"), this.testRepositoryConnection,
                        artifactGraph);
        
        // verify:
        Assert.assertNotNull("Returned NULL inferredOntologyID", inferredOntologyID);
        Assert.assertEquals("Not the expected current version", IRI.create("http://purl.org/podd//99-99/version:1"),
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
    public void testGetOntologiesEmptyOnlyCurrentVersions() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        Collection<InferredOWLOntologyID> ontologies =
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
    public void testGetOntologiesEmptyAllVersions() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        Collection<InferredOWLOntologyID> ontologies =
                this.testPoddSesameManager.getOntologies(true, this.testRepositoryConnection, context);
        
        Assert.assertNotNull(ontologies);
        Assert.assertTrue(ontologies.isEmpty());
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
        final OWLOntologyID emptyOntologyID = new OWLOntologyID();
        
        try
        {
            this.testPoddSesameManager.isPublished(emptyOntologyID, this.testRepositoryConnection);
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
        try
        {
            this.testPoddSesameManager.isPublished(null, this.testRepositoryConnection);
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
        final String testResourcePath = "/test/artifacts/basicProject-1-published.rdf";
        final URI versionUri = ValueFactoryImpl.getInstance().createURI("urn:temp:uuid:artifact:version:55");
        
        final boolean isPublished = this.internalTestIsPublished(testResourcePath, 23, versionUri);
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
        final String testResourcePath = "/test/artifacts/basicProject-1.rdf";
        final URI versionUri = ValueFactoryImpl.getInstance().createURI("urn:temp:artifact:version:1");
        final boolean isPublished = this.internalTestIsPublished(testResourcePath, 23, versionUri);
        Assert.assertEquals("Did not identify artifact as Not Published", false, isPublished);
    }
    
}
