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
package com.github.podd.impl.test;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.util.GraphUtil;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.config.RepositoryConfigSchema;
import org.openrdf.repository.config.RepositoryImplConfig;
import org.openrdf.repository.config.RepositoryImplConfigBase;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.ansell.propertyutil.PropertyUtil;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.test.AbstractPoddOWLManagerTest;
import com.github.podd.api.test.TestConstants;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class PoddOWLManagerImplTest extends AbstractPoddOWLManagerTest
{
    private String reasonerName = "Pellet";
    
    @Override
    protected PoddRepositoryManager getNewPoddRepositoryManagerInstance() throws Exception
    {
        Repository managementRepository = new SailRepository(new MemoryStore(tempDir.newFolder("memorystore")));
        managementRepository.initialize();
        
        final Model graph =
                Rio.parse(this.getClass().getResourceAsStream("/memorystoreconfig.ttl"), "", RDFFormat.TURTLE);
        final Resource repositoryNode = GraphUtil.getUniqueSubject(graph, RepositoryConfigSchema.REPOSITORYTYPE, null);
        final RepositoryImplConfig repositoryImplConfig = RepositoryImplConfigBase.create(graph, repositoryNode);
        Assert.assertNotNull(repositoryImplConfig);
        Assert.assertNotNull(repositoryImplConfig.getType());
        return new PoddRepositoryManagerImpl(managementRepository, repositoryImplConfig, "", tempDir.newFolder(
                "test-podd-repository-manager").toPath(), new PropertyUtil("podd"));
    }
    
    @Override
    protected PoddSchemaManager getNewPoddSchemaManagerInstance()
    {
        return new PoddSchemaManagerImpl();
    }
    
    @Override
    protected PoddSesameManager getNewPoddSesameManagerInstance()
    {
        return new PoddSesameManagerImpl();
    }
    
    @Before
    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        this.testSchemaManager = new PoddSchemaManagerImpl();
        this.testSchemaManager.setOwlManager(this.testOwlManager);
        this.testSchemaManager.setSesameManager(testSesameManager);
        this.testSchemaManager.setRepositoryManager(this.testRepositoryManager);
        
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Override
    @After
    public void tearDown() throws Exception
    {
        this.testRepositoryManager.shutDown();
    }
    
    @Override
    protected OWLReasonerFactory getNewOWLReasonerFactoryInstance()
    {
        return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(this.reasonerName);
    }
    
    @Override
    protected OWLOntologyManagerFactory getNewOWLOntologyManagerFactory()
    {
        final Collection<OWLOntologyManagerFactory> ontologyManagers =
                OWLOntologyManagerFactoryRegistry.getInstance().get(PoddWebConstants.DEFAULT_OWLAPI_MANAGER);
        
        if(ontologyManagers == null || ontologyManagers.isEmpty())
        {
            this.log.error("OWLOntologyManagerFactory was not found");
        }
        return ontologyManagers.iterator().next();
    }
    
    @Override
    protected PoddOWLManagerImpl getNewPoddOWLManagerInstance(final OWLOntologyManagerFactory manager,
            final OWLReasonerFactory reasonerFactory)
    {
        return new PoddOWLManagerImpl(manager, reasonerFactory);
    }
    
    /**
     * Helper method which loads podd:dcTerms, podd:foaf and podd:User schema ontologies.
     */
    @Override
    protected List<InferredOWLOntologyID> loadDcFoafAndPoddUserSchemaOntologies() throws Exception
    {
        return this.testSchemaManager.uploadSchemaOntologies(Rio.parse(
                this.getClass().getResourceAsStream("/podd-schema-manifest-version1only-dcfoafuser.ttl"), "",
                RDFFormat.TURTLE));
    }
    
    /**
     * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
     * 
     * @param resourcePath
     * @param format
     * @param assertedStatements
     * @param inferredStatements
     * @param managementConnection
     * @return
     * @throws Exception
     */
    @Override
    protected InferredOWLOntologyID loadInferStoreOntology(final String resourcePath, final RDFFormat format,
            final long assertedStatements, final long inferredStatements,
            final Set<? extends OWLOntologyID> dependentSchemaOntologies, RepositoryConnection managementConnection)
        throws Exception
    {
        final PoddOWLManagerImpl manager = (PoddOWLManagerImpl)this.testOwlManager;
        
        // load ontology to OWLManager
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        final OWLOntologyDocumentSource owlSource =
                new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                        format.getDefaultMIMEType()));
        
        final InferredOWLOntologyID inferredOntologyID =
                this.testOwlManager.loadAndInfer(owlSource, managementConnection, null, dependentSchemaOntologies,
                        managementConnection, this.schemaGraph);
        
        this.testOwlManager.removeCache(null, dependentSchemaOntologies);
        
        final Set<OWLOntologyID> newDependentSchemaOntologies = new LinkedHashSet<>(dependentSchemaOntologies);
        newDependentSchemaOntologies.add(inferredOntologyID);
        
        manager.cacheSchemaOntologies(newDependentSchemaOntologies, managementConnection, this.schemaGraph);
        
        // verify statement counts
        final URI versionURI = inferredOntologyID.getVersionIRI().toOpenRDFURI();
        Assert.assertEquals("Wrong statement count", assertedStatements, managementConnection.size(versionURI));
        
        final URI inferredOntologyURI = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
        
        // DebugUtils.printContents(testRepositoryConnection, inferredOntologyURI);
        Assert.assertEquals("Wrong inferred statement count", inferredStatements,
                managementConnection.size(inferredOntologyURI));
        
        return inferredOntologyID;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
     * .
     * 
     */
    @Test
    public void testCreateReasonerWithNull() throws Exception
    {
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).createReasoner(null);
            Assert.fail("Should have thrown a Runtime Exception");
        }
        catch(final RuntimeException e)
        {
            Assert.assertTrue("Exception not expected type", e instanceof NullPointerException);
            // this exception is thrown by the OWL API with a null message
        }
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
        // prepare: load an Ontology independently
        final InputStream inputStream = this.getClass().getResourceAsStream(PODD.PATH_PODD_DCTERMS_V1);
        Assert.assertNotNull("Could not find resource", inputStream);
        final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        final OWLOntology loadedOntology = testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
        
        final OWLReasoner reasoner = ((PoddOWLManagerImpl)this.testOwlManager).createReasoner(loadedOntology);
        
        // verify:
        Assert.assertNotNull("Created reasoner was NULL", reasoner);
        Assert.assertEquals(this.getNewOWLReasonerFactoryInstance().getReasonerName(), reasoner.getReasonerName());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
     * .
     * 
     */
    @Test
    public void testCreateReasonerFromEmptyOntology() throws Exception
    {
        // prepare: load an Ontology independently
        final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        final OWLOntology emptyOntology = testOWLOntologyManager.createOntology();
        
        final OWLReasoner reasoner = ((PoddOWLManagerImpl)this.testOwlManager).createReasoner(emptyOntology);
        
        // verify:
        Assert.assertNotNull("Created reasoner was NULL", reasoner);
        Assert.assertEquals(this.getNewOWLReasonerFactoryInstance().getReasonerName(), reasoner.getReasonerName());
    }
    
    /**
     * Test method for {@link com.github.podd.api.PoddOWLManager#getReasonerProfiles()} .
     * 
     */
    @Test
    public void testGetReasonerProfile() throws Exception
    {
        final Set<OWLProfile> profiles = ((PoddOWLManagerImpl)this.testOwlManager).getReasonerProfiles();
        Assert.assertNotNull("OWLProfile was null", profiles);
        Assert.assertFalse("OWLProfiles were not found for reasoner", profiles.isEmpty());
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
        // prepare: create an OntologyID
        final OWLOntologyID ontologyID =
                new OWLOntologyID(IRI.create("http://purl.org/podd/ns/poddBase"),
                        IRI.create("http://purl.org/podd/ns/version/poddBase/1"));
        
        final InferredOWLOntologyID inferredOntologyID =
                ((PoddOWLManagerImpl)this.testOwlManager).generateInferredOntologyID(ontologyID);
        
        // verify:
        Assert.assertNotNull("InferredOntologyID was null", inferredOntologyID);
        Assert.assertNotNull("Inferred Ontology IRI was null", inferredOntologyID.getInferredOntologyIRI());
        Assert.assertEquals("Inferred IRI was not as expected",
                IRI.create(PODD.INFERRED_PREFIX + "http://purl.org/podd/ns/version/poddBase/1"),
                inferredOntologyID.getInferredOntologyIRI());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testGenerateInferredOntologyIDWithEmptyOntologyID() throws Exception
    {
        // prepare: create an OntologyID
        final OWLOntologyID ontologyID = new OWLOntologyID();
        
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).generateInferredOntologyID(ontologyID);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testGenerateInferredOntologyIDWithNullOntologyID() throws Exception
    {
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).generateInferredOntologyID(null);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#loadOntology(org.semanticweb.owlapi.rio.RioMemoryTripleSource)}
     * . Attempts to pass NULL value into loadOntlogy().
     */
    @Test
    public void testLoadOntologyWithNull() throws Exception
    {
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).loadOntologyInternal(null, null, null);
            Assert.fail("Should have thrown a RuntimeException");
        }
        catch(final RuntimeException e)
        {
            Assert.assertTrue("Exception not expected type", e instanceof NullPointerException);
            // this exception is thrown by the OWL API with a null message
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#removeCache(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Ignore("This level of detail is not relevant with multiple managers, so this test may be removed in future")
    @Test
    public void testRemoveCacheWithOntologyNotInMemory() throws Exception
    {
        // prepare: create an ontology externally
        final OWLOntology ontologyLoadedFromMemory =
                OWLOntologyManagerFactoryRegistry.createOWLOntologyManager().createOntology();
        Assert.assertNotNull("Ontology should not be in memory", ontologyLoadedFromMemory);
        
        final OWLOntologyID ontologyID = ontologyLoadedFromMemory.getOntologyID();
        final boolean removed = this.testOwlManager.removeCache(ontologyID, Collections.<OWLOntologyID> emptySet());
        
        // verify:
        Assert.assertFalse("Ontology should not have existed in memory/cache", removed);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#dumpOntologyToRepository(OWLOntology, RepositoryConnection, URI...)}
     * .
     * 
     */
    @Test
    public void testDumpOntologyToRepository() throws Exception
    {
        // prepare: load, infer and store PODD:dcTerms, foaf and User ontologies to testOWLManager
        final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                PODD.PATH_PODD_DCTERMS_V1));
        testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                PODD.PATH_PODD_FOAF_V1));
        testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                PODD.PATH_PODD_USER_V1));
        
        // prepare: load Podd-Base Ontology independently
        final InputStream inputStream = this.getClass().getResourceAsStream(PODD.PATH_PODD_BASE_V1);
        Assert.assertNotNull("Could not find resource", inputStream);
        final OWLOntology nextOntology = testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
        
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:test:dump:context:");
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).dumpOntologyToRepository(nextOntology, managementConnection,
                    context);
            
            // verify:
            Assert.assertEquals("Dumped statement count not expected value",
                    TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE, managementConnection.size(context));
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#dumpOntologyToRepository(OWLOntology, RepositoryConnection, URI...)}
     * .
     * 
     */
    @Ignore("TODO: Enable this test using new methods")
    @Test
    public void testDumpOntologyToRepositoryWithEmptyOntology() throws Exception
    {
        // prepare: load an Ontology independently
        // final OWLOntology nextOntology = this.manager.createOntology();
        final OWLOntology nextOntology = null;
        final RepositoryConnection managementConnection =
                this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).dumpOntologyToRepository(nextOntology, managementConnection);
            Assert.fail("Should have thrown an IllegalArgumentException");
        }
        catch(final IllegalArgumentException e)
        {
            Assert.assertEquals("Cannot dump anonymous ontologies to repository", e.getMessage());
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#dumpOntologyToRepository(OWLOntology, RepositoryConnection, URI...)}
     * .
     * 
     */
    @Test
    public void testDumpOntologyToRepositoryWithoutContext() throws Exception
    {
        // prepare: load, infer and store PODD:dcTerms, foaf and User ontologies to testOWLManager
        final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                PODD.PATH_PODD_DCTERMS_V1));
        testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                PODD.PATH_PODD_FOAF_V1));
        testOWLOntologyManager.loadOntologyFromOntologyDocument(this.getClass().getResourceAsStream(
                PODD.PATH_PODD_USER_V1));
        
        // prepare: load an Ontology independently
        final InputStream inputStream = this.getClass().getResourceAsStream(PODD.PATH_PODD_BASE_V1);
        Assert.assertNotNull("Could not find resource", inputStream);
        final OWLOntology nextOntology = testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).dumpOntologyToRepository(nextOntology, managementConnection);
            
            // verify:
            final URI context = nextOntology.getOntologyID().getVersionIRI().toOpenRDFURI();
            Assert.assertEquals("Dumped statement count not expected value",
                    TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE, managementConnection.size(context));
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    @Test
    public void testBuildTwoLevelOrderedImportsListNull() throws Exception
    {
        final OWLOntologyID ontologyId = new OWLOntologyID();
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).buildTwoLevelOrderedImportsList(null, managementConnection,
                    PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH);
            Assert.fail("Did not receive expected exception");
        }
        catch(final NullPointerException e)
        {
            
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    @Test
    public void testBuildTwoLevelOrderedImportsListAnonymousOntology() throws Exception
    {
        final OWLOntologyID ontologyId = new OWLOntologyID();
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).buildTwoLevelOrderedImportsList(ontologyId, managementConnection,
                    PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH);
            Assert.fail("Did not receive expected exception");
        }
        catch(final NullPointerException e)
        {
            
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    @Test
    public void testBuildTwoLevelOrderedImportsListNonExistentOntologyNoVersion() throws Exception
    {
        final OWLOntologyID ontologyId = new OWLOntologyID(IRI.create("urn:test:doesnotexist"));
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            final List<InferredOWLOntologyID> orderedImportsList =
                    ((PoddOWLManagerImpl)this.testOwlManager).buildTwoLevelOrderedImportsList(ontologyId,
                            managementConnection, PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH);
            
            Assert.assertTrue(orderedImportsList.isEmpty());
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    @Test
    public void testBuildTwoLevelOrderedImportsListNonExistentOntologyWithVersion() throws Exception
    {
        final OWLOntologyID ontologyId =
                new OWLOntologyID(IRI.create("urn:test:doesnotexist"), IRI.create("urn:test:withversion"));
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            final List<InferredOWLOntologyID> orderedImportsList =
                    ((PoddOWLManagerImpl)this.testOwlManager).buildTwoLevelOrderedImportsList(ontologyId,
                            managementConnection, PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH);
            
            Assert.assertTrue(orderedImportsList.isEmpty());
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Ignore("TODO: Improve the way these tests can be performed")
    @Test
    public void testCacheSchemaOntology() throws Exception
    {
        final List<InferredOWLOntologyID> schemaOntologies = this.loadDcFoafAndPoddUserSchemaOntologies();
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            // prepare: load, infer and store PODD-Base ontology
            final InferredOWLOntologyID inferredOntologyID =
                    this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, new LinkedHashSet<>(
                                    schemaOntologies), managementConnection);
            this.testOwlManager.removeCache(null, new LinkedHashSet<>(schemaOntologies));
            schemaOntologies.add(inferredOntologyID);
            
            ((PoddOWLManagerImpl)this.testOwlManager).cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies),
                    managementConnection, null);
            
            // prepare: remove from cache
            this.testOwlManager.removeCache(inferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(inferredOntologyID.getInferredOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            
            Assert.assertFalse("Ontology should not be in memory",
                    this.testOwlManager.isCached(inferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            
            ((PoddOWLManagerImpl)this.testOwlManager).cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies),
                    managementConnection, null);
            
            // verify:
            Assert.assertTrue("Ontology should be in memory", this.testOwlManager.isCached(
                    inferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(schemaOntologies)));
            Assert.assertTrue("Ontology should be in memory", this.testOwlManager.isCached(
                    inferredOntologyID.getInferredOWLOntologyID(), new LinkedHashSet<>(schemaOntologies)));
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Ignore("TODO: Improve the way these tests can be performed")
    @Test
    public void testCacheSchemaOntologyAlreadyInCache() throws Exception
    {
        final List<InferredOWLOntologyID> schemaOntologies = this.loadDcFoafAndPoddUserSchemaOntologies();
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            // prepare: load, infer and store a schema ontology
            final InferredOWLOntologyID inferredOntologyID =
                    this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, new LinkedHashSet<>(
                                    schemaOntologies), managementConnection);
            schemaOntologies.add(inferredOntologyID);
            
            ((PoddOWLManagerImpl)this.testOwlManager).cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies),
                    managementConnection, this.schemaGraph);
            
            Assert.assertTrue("Ontology should already be in memory",
                    this.testOwlManager.isCached(inferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            
            // this call will silently return since the ontology is already in cache
            ((PoddOWLManagerImpl)this.testOwlManager).cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies),
                    managementConnection, null);
            
            // verify:
            Assert.assertTrue("Ontology should still be in memory",
                    this.testOwlManager.isCached(inferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Ignore("TODO: Improve the way these tests can be performed")
    @Test
    public void testCacheSchemaOntologyNotInRepository() throws Exception
    {
        // prepare: a new InferredOWLOntologyID
        final InferredOWLOntologyID inferredOntologyID =
                new InferredOWLOntologyID(IRI.create("http://purl.org/podd/ns/poddBase"),
                        IRI.create("http://purl.org/podd/ns/version/poddBase/1"),
                        IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1"));
        Assert.assertFalse(
                "Ontology should not be in memory",
                this.testOwlManager.isCached(inferredOntologyID.getBaseOWLOntologyID(),
                        Collections.<OWLOntologyID> emptySet()));
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).cacheSchemaOntologies(Collections.singleton(inferredOntologyID),
                    managementConnection, null);
            Assert.fail("Should have thrown an EmptyOntologyException");
        }
        catch(final EmptyOntologyException e)
        {
            Assert.assertEquals("Not the expected Exception", "No statements to create an ontology", e.getMessage());
        }
        finally
        {
            managementConnection.close();
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
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).cacheSchemaOntologies(Collections.singleton(inferredOntologyID),
                    managementConnection, null);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            // Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete",
            // e.getMessage());
        }
        finally
        {
            managementConnection.close();
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
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            ((PoddOWLManagerImpl)this.testOwlManager).cacheSchemaOntologies(null, managementConnection, null);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     */
    @Ignore("TODO: Improve the way these tests can be performed")
    @Test
    public void testCacheSchemaOntologyWithOneImport() throws Exception
    {
        final PoddOWLManagerImpl manager = ((PoddOWLManagerImpl)this.testOwlManager);
        final List<InferredOWLOntologyID> schemaOntologies = this.loadDcFoafAndPoddUserSchemaOntologies();
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            // prepare: 1) load, infer, store PODD-Base ontology
            final InferredOWLOntologyID pbInferredOntologyID =
                    this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, new LinkedHashSet<>(
                                    schemaOntologies), managementConnection);
            this.testOwlManager.removeCache(pbInferredOntologyID, new LinkedHashSet<>(schemaOntologies));
            schemaOntologies.add(pbInferredOntologyID);
            manager.cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies), managementConnection, this.schemaGraph);
            
            final URI pbBaseOntologyURI = pbInferredOntologyID.getOntologyIRI().toOpenRDFURI();
            final URI pbVersionURI = pbInferredOntologyID.getVersionIRI().toOpenRDFURI();
            
            // prepare: 2) load, infer, store PODD-Science ontology
            final InferredOWLOntologyID pScienceInferredOntologyID =
                    this.loadInferStoreOntology(PODD.PATH_PODD_SCIENCE_V1, RDFFormat.RDFXML,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED, new LinkedHashSet<>(
                                    schemaOntologies), managementConnection);
            this.testOwlManager.removeCache(pScienceInferredOntologyID, new LinkedHashSet<>(schemaOntologies));
            schemaOntologies.add(pScienceInferredOntologyID);
            manager.cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies), managementConnection, this.schemaGraph);
            
            final URI pScienceBaseOntologyURI = pScienceInferredOntologyID.getOntologyIRI().toOpenRDFURI();
            final URI pScienceVersionURI = pScienceInferredOntologyID.getVersionIRI().toOpenRDFURI();
            
            // prepare: 3) remove ontologies from manager cache
            this.testOwlManager.removeCache(pbInferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(pbInferredOntologyID.getInferredOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(pScienceInferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(pScienceInferredOntologyID.getInferredOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            
            Assert.assertFalse("Ontology should not be in memory",
                    this.testOwlManager.isCached(pbInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            Assert.assertFalse("Ontology should not be in memory",
                    this.testOwlManager.isCached(pScienceInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            
            // prepare: 4) create schema management graph
            final URI schemaGraph = PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
            
            // Podd-Base
            managementConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pbBaseOntologyURI, OWL.VERSIONIRI, pbVersionURI, schemaGraph);
            managementConnection.add(pbBaseOntologyURI, PODD.OMV_CURRENT_VERSION, pbVersionURI, schemaGraph);
            managementConnection.add(pbVersionURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pbVersionURI, PODD.PODD_BASE_INFERRED_VERSION, pbInferredOntologyID
                    .getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
            
            // Podd-Science
            managementConnection.add(pScienceBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pScienceBaseOntologyURI, OWL.VERSIONIRI, pScienceVersionURI, schemaGraph);
            managementConnection
                    .add(pScienceBaseOntologyURI, PODD.OMV_CURRENT_VERSION, pScienceVersionURI, schemaGraph);
            managementConnection.add(pScienceVersionURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pScienceVersionURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
            managementConnection.add(pScienceVersionURI, PODD.PODD_BASE_INFERRED_VERSION, pScienceInferredOntologyID
                    .getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
            
            // invoke method to test
            ((PoddOWLManagerImpl)this.testOwlManager).cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies),
                    managementConnection, schemaGraph);
            
            // verify:
            Assert.assertTrue("Ontology should be in memory",
                    this.testOwlManager.isCached(pScienceInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            Assert.assertTrue("Ontology should be in memory",
                    this.testOwlManager.isCached(pbInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
        }
        finally
        {
            managementConnection.close();
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#cacheSchemaOntology(com.github.podd.utils.InferredOWLOntologyID, org.openrdf.repository.RepositoryConnection)}
     * .
     * 
     * Tests caching a schema ontology which (for some reason) does not have an inferred Graph in
     * the repository.
     */
    @Ignore("TODO: Improve the way these tests can be performed")
    @Test
    public void testCacheSchemaOntologyWithoutInferences() throws Exception
    {
        final PoddOWLManagerImpl manager = ((PoddOWLManagerImpl)this.testOwlManager);
        final List<InferredOWLOntologyID> schemaOntologies = this.loadDcFoafAndPoddUserSchemaOntologies();
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            final InferredOWLOntologyID inferredOntologyID =
                    this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, new LinkedHashSet<>(
                                    schemaOntologies), managementConnection);
            this.testOwlManager.removeCache(inferredOntologyID, new LinkedHashSet<>(schemaOntologies));
            schemaOntologies.add(inferredOntologyID);
            
            Assert.assertFalse("Ontology should not be in memory",
                    this.testOwlManager.isCached(inferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            
            // invoke method to test
            manager.cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies), managementConnection, null);
            
            // verify:
            Assert.assertTrue("Ontology should be in memory",
                    this.testOwlManager.isCached(inferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
        }
        finally
        {
            managementConnection.close();
        }
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
    @Ignore("TODO: Improve the way these tests can be performed")
    @Test
    public void testCacheSchemaOntologyWithTwoLevelImports() throws Exception
    {
        final PoddOWLManagerImpl manager = ((PoddOWLManagerImpl)this.testOwlManager);
        final List<InferredOWLOntologyID> schemaOntologies = this.loadDcFoafAndPoddUserSchemaOntologies();
        
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            // prepare: 1) load, infer, store PODD-Base ontology
            final InferredOWLOntologyID pbInferredOntologyID =
                    this.loadInferStoreOntology(PODD.PATH_PODD_BASE_V1, RDFFormat.RDFXML,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_INFERRED, new LinkedHashSet<>(
                                    schemaOntologies), managementConnection);
            schemaOntologies.add(pbInferredOntologyID);
            final URI pbBaseOntologyURI = pbInferredOntologyID.getOntologyIRI().toOpenRDFURI();
            final URI pbVersionURI = pbInferredOntologyID.getVersionIRI().toOpenRDFURI();
            
            // prepare: 2) load, infer, store PODD-Science ontology
            final InferredOWLOntologyID pScienceInferredOntologyID =
                    this.loadInferStoreOntology(PODD.PATH_PODD_SCIENCE_V1, RDFFormat.RDFXML,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_CONCRETE,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_SCIENCE_INFERRED, new LinkedHashSet<>(
                                    schemaOntologies), managementConnection);
            schemaOntologies.add(pScienceInferredOntologyID);
            final URI pScienceBaseOntologyURI = pScienceInferredOntologyID.getOntologyIRI().toOpenRDFURI();
            final URI pScienceVersionURI = pScienceInferredOntologyID.getVersionIRI().toOpenRDFURI();
            
            // prepare: 3) load, infer, store PODD-Plant ontology
            final InferredOWLOntologyID pPlantInferredOntologyID =
                    this.loadInferStoreOntology(PODD.PATH_PODD_PLANT_V1, RDFFormat.RDFXML,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_CONCRETE,
                            TestConstants.EXPECTED_TRIPLE_COUNT_PODD_PLANT_INFERRED, new LinkedHashSet<>(
                                    schemaOntologies), managementConnection);
            schemaOntologies.add(pPlantInferredOntologyID);
            final URI pPlantBaseOntologyURI = pPlantInferredOntologyID.getOntologyIRI().toOpenRDFURI();
            final URI pPlantVersionURI = pPlantInferredOntologyID.getVersionIRI().toOpenRDFURI();
            
            // Call method to test
            manager.cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies), managementConnection, this.schemaGraph);
            
            Assert.assertTrue("Ontology should be in memory",
                    this.testOwlManager.isCached(pbInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            Assert.assertTrue("Ontology should be in memory",
                    this.testOwlManager.isCached(pScienceInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            Assert.assertTrue("Ontology should be in memory",
                    this.testOwlManager.isCached(pPlantInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            
            // prepare: 4) remove ontologies from manager cache
            this.testOwlManager.removeCache(pbInferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(pbInferredOntologyID.getInferredOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(pScienceInferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(pScienceInferredOntologyID.getInferredOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(pPlantInferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            this.testOwlManager.removeCache(pPlantInferredOntologyID.getInferredOWLOntologyID(), new LinkedHashSet<>(
                    schemaOntologies));
            
            Assert.assertFalse("Ontology should not be in memory",
                    this.testOwlManager.isCached(pbInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            Assert.assertFalse("Ontology should not be in memory",
                    this.testOwlManager.isCached(pScienceInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            Assert.assertFalse("Ontology should not be in memory",
                    this.testOwlManager.isCached(pPlantInferredOntologyID, new LinkedHashSet<>(schemaOntologies)));
            
            // prepare: 4) create schema management graph
            final URI schemaGraph = PODD.DEFAULT_SCHEMA_MANAGEMENT_GRAPH;
            
            // Podd-Base
            managementConnection.add(pbBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pbBaseOntologyURI, OWL.VERSIONIRI, pbVersionURI, schemaGraph);
            managementConnection.add(pbBaseOntologyURI, PODD.OMV_CURRENT_VERSION, pbVersionURI, schemaGraph);
            managementConnection.add(pbVersionURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pbVersionURI, PODD.PODD_BASE_INFERRED_VERSION, pbInferredOntologyID
                    .getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
            
            // Podd-Science
            managementConnection.add(pScienceBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pScienceBaseOntologyURI, OWL.VERSIONIRI, pScienceVersionURI, schemaGraph);
            managementConnection
                    .add(pScienceBaseOntologyURI, PODD.OMV_CURRENT_VERSION, pScienceVersionURI, schemaGraph);
            managementConnection.add(pScienceVersionURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pScienceVersionURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
            managementConnection.add(pScienceVersionURI, PODD.PODD_BASE_INFERRED_VERSION, pScienceInferredOntologyID
                    .getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
            
            // Podd-Plant
            managementConnection.add(pPlantBaseOntologyURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pPlantBaseOntologyURI, OWL.VERSIONIRI, pPlantVersionURI, schemaGraph);
            managementConnection.add(pPlantBaseOntologyURI, PODD.OMV_CURRENT_VERSION, pPlantVersionURI, schemaGraph);
            managementConnection.add(pPlantVersionURI, RDF.TYPE, OWL.ONTOLOGY, schemaGraph);
            managementConnection.add(pPlantVersionURI, OWL.IMPORTS, pScienceVersionURI, schemaGraph);
            managementConnection.add(pPlantVersionURI, OWL.IMPORTS, pbVersionURI, schemaGraph);
            managementConnection.add(pPlantVersionURI, PODD.PODD_BASE_INFERRED_VERSION, pPlantInferredOntologyID
                    .getInferredOntologyIRI().toOpenRDFURI(), schemaGraph);
            
            // invoke method to test
            manager.cacheSchemaOntologies(new LinkedHashSet<>(schemaOntologies), managementConnection, schemaGraph);
            
            // verify:
            Assert.assertTrue("Ontology should be in memory", this.testOwlManager.isCached(
                    pScienceInferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(schemaOntologies)));
            // FIXME: Inferred Ontologies are not being loaded into memory, is this the desired
            // mode?
            // Assert.assertTrue("Ontology should be in memory",
            // this.testOWLManager.isCached(pScienceInferredOntologyID.getInferredOWLOntologyID()));
            Assert.assertTrue("Ontology should be in memory", this.testOwlManager.isCached(
                    pbInferredOntologyID.getBaseOWLOntologyID(), new LinkedHashSet<>(schemaOntologies)));
        }
        finally
        {
            managementConnection.close();
        }
    }
    
}
