/**
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
package com.github.podd.api.test;

import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;

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
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected PoddOWLManager testOWLManager;
    
    private Repository testRepository;
    
    protected RepositoryConnection testRepositoryConnection;
    
    protected OWLOntologyManager manager;
    
    /**
     * @return A new OWLReasonerFactory instance for use with the PoddOWLManager
     */
    protected abstract OWLReasonerFactory getNewOWLReasonerFactoryInstance();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link OWLOntologyManagerFactory} that can be used with the {@link PoddOWLManager}.
     * 
     * @return A new empty instance of an implementation of {@link OWLOntologyManagerFactory}.
     */
    protected abstract OWLOntologyManagerFactory getNewOWLOntologyManagerFactory();
    
    /**
     * @return A new instance of PoddOWLManager, for each call to this method
     */
    protected abstract PoddOWLManager getNewPoddOWLManagerInstance(OWLOntologyManagerFactory nextManager,
            OWLReasonerFactory nextReasonerFactory);
    
    protected OWLOntology independentlyLoadOntology(final OWLOntologyManager testOWLOntologyManager,
            final String resourcePath) throws Exception
    {
        final InputStream inputStream = this.getClass().getResourceAsStream(PODD.PATH_PODD_BASE_V1);
        Assert.assertNotNull("Could not find resource", inputStream);
        return testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
    }
    
    /**
     * Helper method which loads podd:dcTerms, podd:foaf and podd:User schema ontologies.
     */
    protected void loadDcFoafAndPoddUserSchemaOntologies() throws Exception
    {
        this.loadInferStoreOntology(PODD.PATH_PODD_DCTERMS_V1, RDFFormat.RDFXML,
                TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_CONCRETE,
                TestConstants.EXPECTED_TRIPLE_COUNT_DC_TERMS_INFERRED);
        this.loadInferStoreOntology(PODD.PATH_PODD_FOAF_V1, RDFFormat.RDFXML,
                TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_CONCRETE, TestConstants.EXPECTED_TRIPLE_COUNT_FOAF_INFERRED);
        this.loadInferStoreOntology(PODD.PATH_PODD_USER_V1, RDFFormat.RDFXML,
                TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_CONCRETE,
                TestConstants.EXPECTED_TRIPLE_COUNT_PODD_USER_INFERRED);
    }
    
    /**
     * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
     * 
     * @param resourcePath
     * @param format
     * @param assertedStatements
     * @param inferredStatements
     * @return
     * @throws Exception
     */
    protected InferredOWLOntologyID loadInferStoreOntology(final String resourcePath, final RDFFormat format,
            final long assertedStatements, final long inferredStatements) throws Exception
    {
        // load ontology to OWLManager
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        final OWLOntologyDocumentSource owlSource =
                new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                        format.getDefaultMIMEType()));
        
        final InferredOWLOntologyID inferredOntologyID =
                this.testOWLManager.loadAndInfer(owlSource, this.testRepositoryConnection, null);
        
        // verify statement counts
        final URI versionURI = inferredOntologyID.getVersionIRI().toOpenRDFURI();
        Assert.assertEquals("Wrong statement count", assertedStatements, this.testRepositoryConnection.size(versionURI));
        
        final URI inferredOntologyURI = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
        
        // DebugUtils.printContents(testRepositoryConnection, inferredOntologyURI);
        Assert.assertEquals("Wrong inferred statement count", inferredStatements,
                this.testRepositoryConnection.size(inferredOntologyURI));
        
        return inferredOntologyID;
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        // Assert.assertNotNull("Null implementation of OWLOntologymanager", this.manager);
        
        final OWLReasonerFactory reasonerFactory = this.getNewOWLReasonerFactoryInstance();
        Assert.assertNotNull("Null implementation of reasoner factory", reasonerFactory);
        
        final OWLOntologyManagerFactory managerFactory = this.getNewOWLOntologyManagerFactory();
        
        this.testOWLManager = this.getNewPoddOWLManagerInstance(managerFactory, reasonerFactory);
        Assert.assertNotNull("Null implementation of test OWLManager", this.testOWLManager);
        
        // create a memory Repository for tests
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        this.testRepositoryConnection = this.testRepository.getConnection();
        // this.testRepositoryConnection.begin();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        // this.testRepositoryConnection.rollback();
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
    @Test
    public void testCacheSchemaOntology() throws Exception
    {
        this.loadDcFoafAndPoddUserSchemaOntologies();
        
        // prepare: load, infer and store PODD-Base ontology
        final InferredOWLOntologyID inferredOntologyID =
                this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
        
        // prepare: remove from cache
        this.testOWLManager.removeCache(inferredOntologyID.getBaseOWLOntologyID());
        this.testOWLManager.removeCache(inferredOntologyID.getInferredOWLOntologyID());
        
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(inferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(inferredOntologyID.getInferredOntologyIRI()));
        
        this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
        
        // verify:
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(inferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(inferredOntologyID.getInferredOWLOntologyID()));
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Test
    public void testCacheSchemaOntologyAlreadyInCache() throws Exception
    {
        this.loadDcFoafAndPoddUserSchemaOntologies();
        
        // prepare: load, infer and store a schema ontology
        final InferredOWLOntologyID inferredOntologyID =
                this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
        
        Assert.assertNotNull("Ontology should already be in memory", this.manager.getOntology(inferredOntologyID));
        
        // this call will silently return since the ontology is already in cache
        this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
        
        // verify:
        Assert.assertNotNull("Ontology should still be in memory", this.manager.getOntology(inferredOntologyID));
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Test
    public void testCacheSchemaOntologyNotInRepository() throws Exception
    {
        // prepare: a new InferredOWLOntologyID
        final InferredOWLOntologyID inferredOntologyID =
                new InferredOWLOntologyID(IRI.create("http://purl.org/podd/ns/poddBase"),
                        IRI.create("http://purl.org/podd/ns/version/poddBase/1"),
                        IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1"));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(inferredOntologyID.getBaseOWLOntologyID()));
        
        try
        {
            this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
            Assert.fail("Should have thrown an EmptyOntologyException");
        }
        catch(final EmptyOntologyException e)
        {
            Assert.assertEquals("Not the expected Exception", "No statements to create an ontology", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Test
    public void testCacheSchemaOntologyWithEmptyOntologyID() throws Exception
    {
        final InferredOWLOntologyID inferredOntologyID = new InferredOWLOntologyID((IRI)null, null, null);
        
        try
        {
            this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * . E.g. Test caching schema ontology "A" A :imports B B :imports C C :imports D
     */
    @Ignore
    @Test
    public void testCacheSchemaOntologyWithIndirectImports() throws Exception
    {
        Assert.fail("TODO: implement me");
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Test
    public void testCacheSchemaOntologyWithNullOntologyID() throws Exception
    {
        try
        {
            this.testOWLManager.cacheSchemaOntology(null, this.testRepositoryConnection, null);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Test
    public void testCacheSchemaOntologyWithOneImport() throws Exception
    {
        this.loadDcFoafAndPoddUserSchemaOntologies();
        
        // prepare: 1) load, infer, store PODD-Base ontology
        final InferredOWLOntologyID pbInferredOntologyID =
                this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
        final URI pbBaseOntologyURI = pbInferredOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI pbVersionURI = pbInferredOntologyID.getVersionIRI().toOpenRDFURI();
        
        // prepare: 2) load, infer, store PODD-Science ontology
        final InferredOWLOntologyID pScienceInferredOntologyID =
                this.loadInferStoreOntology(PODD.PATH_PODD_SCIENCE_V1, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED);
        final URI pScienceBaseOntologyURI = pScienceInferredOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI pScienceVersionURI = pScienceInferredOntologyID.getVersionIRI().toOpenRDFURI();
        
        // prepare: 3) remove ontologies from manager cache
        this.testOWLManager.removeCache(pbInferredOntologyID.getBaseOWLOntologyID());
        this.testOWLManager.removeCache(pbInferredOntologyID.getInferredOWLOntologyID());
        this.testOWLManager.removeCache(pScienceInferredOntologyID.getBaseOWLOntologyID());
        this.testOWLManager.removeCache(pScienceInferredOntologyID.getInferredOWLOntologyID());
        
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pbInferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pScienceInferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pScienceInferredOntologyID.getInferredOntologyIRI()));
        
        // prepare: 4) create schema management graph
        final URI schemaGraph = PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
        
        // Podd-Base
        this.testRepositoryConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PODD.OWL_VERSION_IRI, pbVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION,
                pbInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
        
        // Podd-Science
        this.testRepositoryConnection.add(pScienceBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
        this.testRepositoryConnection.add(pScienceBaseOntologyURI, PODD.OWL_VERSION_IRI, pScienceVersionURI,
                schemaGraph);
        this.testRepositoryConnection.add(pScienceBaseOntologyURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pScienceBaseOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION,
                pScienceInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
        
        // invoke method to test
        this.testOWLManager.cacheSchemaOntology(pScienceInferredOntologyID, this.testRepositoryConnection, schemaGraph);
        
        // verify:
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(pScienceInferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(pbInferredOntologyID.getInferredOntologyIRI()));
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(pScienceInferredOntologyID.getInferredOWLOntologyID()));
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(pbInferredOntologyID.getBaseOWLOntologyID()));
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     * Tests caching a schema ontology which (for some reason) does not have an inferred Graph in
     * the repository.
     */
    @Test
    public void testCacheSchemaOntologyWithoutInferences() throws Exception
    {
        this.loadDcFoafAndPoddUserSchemaOntologies();
        
        final InferredOWLOntologyID inferredOntologyID =
                this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
        
        this.testOWLManager.removeCache(inferredOntologyID);
        
        Assert.assertFalse("Ontology should not be in memory", this.manager.contains(inferredOntologyID));
        
        // invoke method to test
        this.testOWLManager.cacheSchemaOntology(inferredOntologyID, this.testRepositoryConnection, null);
        
        // verify:
        Assert.assertTrue("Ontology should be in memory", this.manager.contains(inferredOntologyID));
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     * Tests the following hierarchy of imports when caching PoddPlant schema ontology.
     * 
     * PoddPlant :imports PoddScience :imports PoddBase PoddScience :imports PoddBase
     */
    @Test
    public void testCacheSchemaOntologyWithTwoLevelImports() throws Exception
    {
        this.loadDcFoafAndPoddUserSchemaOntologies();
        
        // prepare: 1) load, infer, store PODD-Base ontology
        final InferredOWLOntologyID pbInferredOntologyID =
                this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED);
        final URI pbBaseOntologyURI = pbInferredOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI pbVersionURI = pbInferredOntologyID.getVersionIRI().toOpenRDFURI();
        
        // prepare: 2) load, infer, store PODD-Science ontology
        final InferredOWLOntologyID pScienceInferredOntologyID =
                this.loadInferStoreOntology(PODD.PATH_PODD_SCIENCE_V1, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED);
        final URI pScienceBaseOntologyURI = pScienceInferredOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI pScienceVersionURI = pScienceInferredOntologyID.getVersionIRI().toOpenRDFURI();
        
        // prepare: 3) load, infer, store PODD-Plant ontology
        final InferredOWLOntologyID pPlantInferredOntologyID =
                this.loadInferStoreOntology(PODD.PATH_PODD_PLANT_V1, RDFFormat.RDFXML,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_CONCRETE,
                        TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_INFERRED);
        final URI pPlantBaseOntologyURI = pPlantInferredOntologyID.getOntologyIRI().toOpenRDFURI();
        final URI pPlantVersionURI = pPlantInferredOntologyID.getVersionIRI().toOpenRDFURI();
        
        // prepare: 4) remove ontologies from manager cache
        this.testOWLManager.removeCache(pbInferredOntologyID.getBaseOWLOntologyID());
        this.testOWLManager.removeCache(pbInferredOntologyID.getInferredOWLOntologyID());
        this.testOWLManager.removeCache(pScienceInferredOntologyID.getBaseOWLOntologyID());
        this.testOWLManager.removeCache(pScienceInferredOntologyID.getInferredOWLOntologyID());
        this.testOWLManager.removeCache(pPlantInferredOntologyID.getBaseOWLOntologyID());
        this.testOWLManager.removeCache(pPlantInferredOntologyID.getInferredOWLOntologyID());
        
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pbInferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pbInferredOntologyID.getInferredOntologyIRI()));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pScienceInferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pScienceInferredOntologyID.getInferredOntologyIRI()));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pPlantInferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertFalse("Ontology should not be in memory",
                this.manager.contains(pPlantInferredOntologyID.getInferredOntologyIRI()));
        
        // prepare: 4) create schema management graph
        final URI schemaGraph = PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
        
        // Podd-Base
        this.testRepositoryConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PODD.OWL_VERSION_IRI, pbVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pbBaseOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION,
                pbInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
        
        // Podd-Science
        this.testRepositoryConnection.add(pScienceBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
        this.testRepositoryConnection.add(pScienceBaseOntologyURI, PODD.OWL_VERSION_IRI, pScienceVersionURI,
                schemaGraph);
        this.testRepositoryConnection.add(pScienceBaseOntologyURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pScienceBaseOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION,
                pScienceInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
        
        // Podd-Plant
        this.testRepositoryConnection.add(pPlantBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
        this.testRepositoryConnection.add(pPlantBaseOntologyURI, PODD.OWL_VERSION_IRI, pPlantVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pPlantBaseOntologyURI, OWL.IMPORTS, pScienceVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pPlantBaseOntologyURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
        this.testRepositoryConnection.add(pPlantBaseOntologyURI, PODD.PODD_BASE_CURRENT_INFERRED_VERSION,
                pPlantInferredOntologyID.getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
        
        // invoke method to test
        this.testOWLManager.cacheSchemaOntology(pPlantInferredOntologyID, this.testRepositoryConnection, schemaGraph);
        
        // verify:
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(pScienceInferredOntologyID.getBaseOWLOntologyID()));
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(pScienceInferredOntologyID.getInferredOWLOntologyID()));
        Assert.assertTrue("Ontology should be in memory",
                this.manager.contains(pbInferredOntologyID.getBaseOWLOntologyID()));
    }
    
}
