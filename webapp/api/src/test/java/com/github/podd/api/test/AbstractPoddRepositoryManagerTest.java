/**
 * 
 */
package com.github.podd.api.test;

import info.aduna.iteration.Iterations;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryResult;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
@Ignore
public abstract class AbstractPoddRepositoryManagerTest
{
    
    private PoddRepositoryManager testRepositoryManager;
    
    /**
     * @return A new instance of PoddOWLManager, for each call to this method
     */
    protected abstract PoddRepositoryManager getNewPoddRepositoryManagerInstance();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testRepositoryManager = this.getNewPoddRepositoryManagerInstance();
        
        this.testRepositoryManager.setSchemaManagementGraph(ValueFactoryImpl.getInstance().createURI(
                "urn:test:schema-graph"));
        this.testRepositoryManager.setArtifactManagementGraph(ValueFactoryImpl.getInstance().createURI(
                "urn:test:artifact-graph"));
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testRepositoryManager.getRepository().shutDown();
        this.testRepositoryManager = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getArtifactManagementGraph()}.
     */
    @Test
    public final void testGetArtifactManagementGraph() throws Exception
    {
        Assert.assertNotNull("Artifact management graph was null",
                this.testRepositoryManager.getArtifactManagementGraph());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getNewTemporaryRepository()}.
     */
    @Test
    public final void testGetNewTemporaryRepository() throws Exception
    {
        Repository newTempRepository = null;
        RepositoryConnection tempRepositoryConnection = null;
        try
        {
            newTempRepository = this.testRepositoryManager.getNewTemporaryRepository();
            Assert.assertNotNull("New temporary repository was null", newTempRepository);
            Assert.assertTrue("New temporary repository was not initialized", newTempRepository.isInitialized());
            tempRepositoryConnection = newTempRepository.getConnection();
            tempRepositoryConnection.begin();
            Assert.assertEquals("New temporary repository was not empty", 0, tempRepositoryConnection.size());
        }
        finally
        {
            if(tempRepositoryConnection != null && tempRepositoryConnection.isActive())
            {
                tempRepositoryConnection.rollback();
                tempRepositoryConnection.close();
            }
            if(newTempRepository != null)
            {
                newTempRepository.shutDown();
            }
        }
    }
    
    /**
     * Test method for {@link com.github.podd.impl.PoddRepositoryManagerImpl#getRepository()}.
     */
    @Test
    public final void testGetRepository() throws Exception
    {
        Assert.assertNotNull("Repository was null", this.testRepositoryManager.getRepository());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#getSchemaManagementGraph()}.
     */
    @Test
    public final void testGetSchemaManagementGraph() throws Exception
    {
        Assert.assertNotNull("Schema management graph was null", this.testRepositoryManager.getSchemaManagementGraph());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#setArtifactManagementGraph(org.openrdf.model.URI)}
     * .
     */
    @Test
    public final void testSetArtifactManagementGraph() throws Exception
    {
        final URI testArtifactMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-graph");
        this.testRepositoryManager.setArtifactManagementGraph(testArtifactMgtGraph);
        Assert.assertEquals("Artifact graph was not correctly set", testArtifactMgtGraph,
                this.testRepositoryManager.getArtifactManagementGraph());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#setSchemaManagementGraph(org.openrdf.model.URI)}
     * .
     */
    @Test
    public final void testSetSchemaManagementGraph() throws Exception
    {
        final URI testSchemaMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:my-test:schema-management-graph");
        this.testRepositoryManager.setSchemaManagementGraph(testSchemaMgtGraph);
        Assert.assertEquals("Schema graph was not correctly set", testSchemaMgtGraph,
                this.testRepositoryManager.getSchemaManagementGraph());
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
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyID.getBaseOWLOntologyID(),
                nextOntologyID.getInferredOWLOntologyID(), false);
        
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getSchemaManagementGraph(), pOntologyIRI,
                pVersionIRI, pInferredVersionIRI);
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
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyID.getBaseOWLOntologyID(),
                nextOntologyID.getInferredOWLOntologyID(), false);
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getSchemaManagementGraph(), pOntologyIRI,
                pVersionIRI, pInferredVersionIRI);
        
        final IRI pVersionIRIUpdated = IRI.create("http://purl.org/podd/ns/version/poddBase/4");
        final IRI pInferredVersionIRIUpdated = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/5");
        final InferredOWLOntologyID nextOntologyIDUpdated =
                new InferredOWLOntologyID(pOntologyIRI, pVersionIRIUpdated, pInferredVersionIRIUpdated);
        
        // invoke with "updateCurrent" disallowed
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(
                nextOntologyIDUpdated.getBaseOWLOntologyID(), nextOntologyIDUpdated.getInferredOWLOntologyID(), false);
        
        // verify only inferred ontology version is updated
        this.verifyManagementGraphContents(9, this.testRepositoryManager.getSchemaManagementGraph(), pOntologyIRI,
                pVersionIRI, pInferredVersionIRIUpdated);
        
        // invoke with "updateCurrent" allowed
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(
                nextOntologyIDUpdated.getBaseOWLOntologyID(), nextOntologyIDUpdated.getInferredOWLOntologyID(), true);
        
        // verify both ontology current version and inferred ontology version haven been updated
        this.verifyManagementGraphContents(9, this.testRepositoryManager.getSchemaManagementGraph(), pOntologyIRI,
                pVersionIRIUpdated, pInferredVersionIRIUpdated);
        
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
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyID.getBaseOWLOntologyID(),
                nextOntologyID.getInferredOWLOntologyID(), false);
        
        // verify:
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRI, pInferredVersionIRI);
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
        
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv1.getBaseOWLOntologyID(),
                nextOntologyIDv1.getInferredOWLOntologyID(), false);
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersion1IRIv1, pInferredVersionIRIv1);
        
        // prepare: update artifact version
        final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
        final InferredOWLOntologyID nextOntologyIDv2 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        // invoke method under test
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv2.getBaseOWLOntologyID(),
                nextOntologyIDv2.getInferredOWLOntologyID(), true);
        
        // verify:
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRIv2, pInferredVersionIRIv2);
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
        
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv1.getBaseOWLOntologyID(),
                nextOntologyIDv1.getInferredOWLOntologyID(), false);
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRIv1, pInferredVersionIRIv1);
        
        // prepare: version 2
        final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
        final InferredOWLOntologyID nextOntologyIDv2 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        // invoke with "updateCurrent" disallowed
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv2.getBaseOWLOntologyID(),
                nextOntologyIDv2.getInferredOWLOntologyID(), false);
        
        // verify: only inferred version is updated
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRIv1, pInferredVersionIRIv2);
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
        
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv1.getBaseOWLOntologyID(),
                nextOntologyIDv1.getInferredOWLOntologyID(), false);
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRIv1, pInferredVersionIRIv1);
        
        // prepare: version 2
        final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
        final InferredOWLOntologyID nextOntologyIDv2 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        // invoke with "updateCurrent" disallowed
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv2.getBaseOWLOntologyID(),
                nextOntologyIDv2.getInferredOWLOntologyID(), false);
        
        // verify: only inferred version is updated
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRIv1, pInferredVersionIRIv2);
        
        // update to version 3
        final IRI pVersionIRIv3 = IRI.create("http://purl.org/abc-def/artifact:1:version:3");
        final IRI pInferredVersionIRIv3 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:3");
        final InferredOWLOntologyID nextOntologyIDv3 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv3, pInferredVersionIRIv3);
        
        // invoke with "updateCurrent" allowed
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv3.getBaseOWLOntologyID(),
                nextOntologyIDv3.getInferredOWLOntologyID(), true);
        
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRIv3, pInferredVersionIRIv3);
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
        final RepositoryConnection repositoryConnection = this.testRepositoryManager.getRepository().getConnection();
        try
        {
            repositoryConnection.begin();
            
            final URI subject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
            repositoryConnection.add(subject, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                    PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pVersionIRIv1.toOpenRDFURI());
            
            final URI inferredSubject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
            repositoryConnection.add(inferredSubject, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                    PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pInferredVersionIRIv1.toOpenRDFURI());
            
            // verify: contexts populated for test artifact
            Assert.assertEquals("Asserted graph should have 1 statement", 1,
                    repositoryConnection.size(pVersionIRIv1.toOpenRDFURI()));
            Assert.assertEquals("Inferred graph should have 1 statement", 1,
                    repositoryConnection.size(pInferredVersionIRIv1.toOpenRDFURI()));
        }
        finally
        {
            repositoryConnection.commit();
            repositoryConnection.close();
        }
        
        // invoke method under test
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv1.getBaseOWLOntologyID(),
                nextOntologyIDv1.getInferredOWLOntologyID(), false);
        // verify: artifact management graph
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRIv1, pInferredVersionIRIv1);
        
        // prepare: version 2 of test artifact
        final IRI pVersionIRIv2 = IRI.create("http://purl.org/abc-def/artifact:1:version:2");
        final IRI pInferredVersionIRIv2 = IRI.create("urn:inferred:http://purl.org/abc-def/artifact:1:version:2");
        final InferredOWLOntologyID nextOntologyIDv2 =
                new InferredOWLOntologyID(pArtifactIRI, pVersionIRIv2, pInferredVersionIRIv2);
        
        // prepare: add dummy statements in relevant contexts for version 2 of test artifact
        final RepositoryConnection nextRepositoryConnection =
                this.testRepositoryManager.getRepository().getConnection();
        try
        {
            nextRepositoryConnection.begin();
            
            final URI subject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
            nextRepositoryConnection.add(subject, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                    PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pVersionIRIv2.toOpenRDFURI());
            
            final URI inferredSubject = ValueFactoryImpl.getInstance().createURI("http://purl.org/abc-def/artifact:1");
            nextRepositoryConnection.add(inferredSubject, PoddRdfConstants.PODDBASE_HAS_PUBLICATION_STATUS,
                    PoddRdfConstants.PODDBASE_NOT_PUBLISHED, pInferredVersionIRIv2.toOpenRDFURI());
            
            // verify: contexts populated for test artifact
            Assert.assertEquals("Asserted graph should have 1 statement", 1,
                    nextRepositoryConnection.size(pVersionIRIv2.toOpenRDFURI()));
            Assert.assertEquals("Inferred graph should have 1 statement", 1,
                    nextRepositoryConnection.size(pInferredVersionIRIv2.toOpenRDFURI()));
        }
        finally
        {
            nextRepositoryConnection.commit();
            nextRepositoryConnection.close();
        }
        
        // invoke method under test
        this.testRepositoryManager.updateManagedPoddArtifactVersion(nextOntologyIDv2.getBaseOWLOntologyID(),
                nextOntologyIDv2.getInferredOWLOntologyID(), true);
        
        // verify:
        this.verifyManagementGraphContents(6, this.testRepositoryManager.getArtifactManagementGraph(), pArtifactIRI,
                pVersionIRIv2, pInferredVersionIRIv2);
        
        // verify: contexts for previous version deleted from repository
        final RepositoryConnection thirdRepositoryConnection =
                this.testRepositoryManager.getRepository().getConnection();
        try
        {
            thirdRepositoryConnection.begin();
            Assert.assertEquals("Old asserted graph should be deleted", 0,
                    thirdRepositoryConnection.size(pVersionIRIv1.toOpenRDFURI()));
            Assert.assertEquals("Old inferred graph should be deleted", 0,
                    thirdRepositoryConnection.size(pInferredVersionIRIv1.toOpenRDFURI()));
        }
        finally
        {
            thirdRepositoryConnection.commit();
            thirdRepositoryConnection.close();
        }
    }
    
    /**
     * Helper method to verify the contents of a management graph
     * 
     * @param graphSize
     *            Expected size of the graph
     * @param testGraph
     *            The Graph/context to be tested
     * @param pOntologyIRI
     *            The ontology/artifact
     * @param pVersionIRI
     *            Version IRI of the ontology/artifact
     * @param pInferredVersionIRI
     *            Inferred version of the ontology/artifact
     * @throws Exception
     */
    private void verifyManagementGraphContents(final int graphSize, final URI testGraph, final IRI pOntologyIRI,
            final IRI pVersionIRI, final IRI pInferredVersionIRI) throws Exception
    {
        final RepositoryConnection repositoryConnection = this.testRepositoryManager.getRepository().getConnection();
        try
        {
            repositoryConnection.begin();
            Assert.assertEquals("Graph not of expected size", graphSize, repositoryConnection.size(testGraph));
            
            final RepositoryResult<Statement> statements =
                    repositoryConnection.getStatements(null, PoddRdfConstants.OMV_CURRENT_VERSION, null, false,
                            testGraph);
            final List<Statement> stmtList = Iterations.asList(statements);
            Assert.assertEquals("Graph should have one OMV_CURRENT_VERSION statement", 1, stmtList.size());
            Assert.assertEquals("Wrong ontology IRI", pOntologyIRI.toString(), stmtList.get(0).getSubject().toString());
            Assert.assertEquals("Wrong version IRI", pVersionIRI.toString(), stmtList.get(0).getObject().toString());
            
            final RepositoryResult<Statement> inferredVersionStatements =
                    repositoryConnection.getStatements(null, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null,
                            false, testGraph);
            final List<Statement> inferredVersionStatementList = Iterations.asList(inferredVersionStatements);
            Assert.assertEquals("Graph should have one CURRENT_INFERRED_VERSION statement", 1,
                    inferredVersionStatementList.size());
            Assert.assertEquals("Wrong ontology IRI", pOntologyIRI.toString(), inferredVersionStatementList.get(0)
                    .getSubject().toString());
            Assert.assertEquals("Wrong version IRI", pInferredVersionIRI.toString(), inferredVersionStatementList
                    .get(0).getObject().toString());
        }
        finally
        {
            repositoryConnection.rollback();
            repositoryConnection.close();
        }
    }
    
}
