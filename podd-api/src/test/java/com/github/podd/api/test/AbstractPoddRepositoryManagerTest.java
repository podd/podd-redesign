/**
 * 
 */
package com.github.podd.api.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.OpenRDFException;
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
        final URI testSchemaMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-graph");
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
        final URI testSchemaMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-graph");
        this.testRepositoryManager.setSchemaManagementGraph(testSchemaMgtGraph);
        
        final IRI pOntologyIRI = IRI.create("http://purl.org/podd/ns/poddBase");
        final IRI pVersionIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/1");
        final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
        final InferredOWLOntologyID nextOntologyID =
                new InferredOWLOntologyID(pOntologyIRI, pVersionIRI, pInferredVersionIRI);
        
        // invoke method under test
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyID.getBaseOWLOntologyID(),
                nextOntologyID.getInferredOWLOntologyID(), false);
        
        this.verifySchemaManagementGraphContents(6, testSchemaMgtGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRI);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateCurrentManagedSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     */
    @Test
    public final void testUpdateCurrentManagedSchemaOntologyVersionWithUpdate() throws Exception
    {
        final URI testSchemaMgtGraph = ValueFactoryImpl.getInstance().createURI("urn:test:schema-graph");
        this.testRepositoryManager.setSchemaManagementGraph(testSchemaMgtGraph);
        
        final IRI pOntologyIRI = IRI.create("http://purl.org/podd/ns/poddBase");
        final IRI pVersionIRI = IRI.create("http://purl.org/podd/ns/version/poddBase/1");
        final IRI pInferredVersionIRI = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/1");
        final InferredOWLOntologyID nextOntologyID =
                new InferredOWLOntologyID(pOntologyIRI, pVersionIRI, pInferredVersionIRI);
        
        // first setting of schema versions in mgt graph
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(nextOntologyID.getBaseOWLOntologyID(),
                nextOntologyID.getInferredOWLOntologyID(), false);
        this.verifySchemaManagementGraphContents(6, testSchemaMgtGraph, pOntologyIRI, pVersionIRI, pInferredVersionIRI);
        
        final IRI pVersionIRIUpdated = IRI.create("http://purl.org/podd/ns/version/poddBase/4");
        final IRI pInferredVersionIRIUpdated = IRI.create("urn:inferred:http://purl.org/podd/ns/version/poddBase/5");
        final InferredOWLOntologyID nextOntologyIDUpdated =
                new InferredOWLOntologyID(pOntologyIRI, pVersionIRIUpdated, pInferredVersionIRIUpdated);
        
        // invoke with "updateCurrent" disallowed
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(
                nextOntologyIDUpdated.getBaseOWLOntologyID(), nextOntologyIDUpdated.getInferredOWLOntologyID(), false);
        
        // verify only inferred ontology version is updated
        this.verifySchemaManagementGraphContents(9, testSchemaMgtGraph, pOntologyIRI, pVersionIRI,
                pInferredVersionIRIUpdated);
        
        // invoke with "updateCurrent" allowed
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(
                nextOntologyIDUpdated.getBaseOWLOntologyID(), nextOntologyIDUpdated.getInferredOWLOntologyID(), true);
        
        // verify both ontology current version and inferred ontology version haven been updated
        this.verifySchemaManagementGraphContents(9, testSchemaMgtGraph, pOntologyIRI, pVersionIRIUpdated,
                pInferredVersionIRIUpdated);
        
    }
    
    private void verifySchemaManagementGraphContents(final int graphSize, final URI testSchemaMgtGraph,
            final IRI pOntologyIRI, final IRI pVersionIRI, final IRI pInferredVersionIRI) throws OpenRDFException
    {
        final RepositoryConnection repositoryConnection = this.testRepositoryManager.getRepository().getConnection();
        try
        {
            repositoryConnection.begin();
            Assert.assertEquals("Schema graph not of expected size", graphSize, repositoryConnection.size());
            
            final RepositoryResult<Statement> statements =
                    repositoryConnection.getStatements(null, PoddRdfConstants.OMV_CURRENT_VERSION, null, false,
                            testSchemaMgtGraph);
            final List<Statement> stmtList = statements.asList();
            Assert.assertEquals("Schema graph should have one OMV_CURRENT_VERSION statement", 1, stmtList.size());
            Assert.assertEquals("Wrong ontology IRI", pOntologyIRI.toString(), stmtList.get(0).getSubject().toString());
            Assert.assertEquals("Wrong version IRI", pVersionIRI.toString(), stmtList.get(0).getObject().toString());
            
            final RepositoryResult<Statement> inferredVersionStatements =
                    repositoryConnection.getStatements(null, PoddRdfConstants.PODD_BASE_CURRENT_INFERRED_VERSION, null,
                            false, testSchemaMgtGraph);
            final List<Statement> inferredVersionStatementList = inferredVersionStatements.asList();
            Assert.assertEquals("Schema graph should have one CURRENT_INFERRED_VERSION statement", 1,
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
    
    /**
     * Test method for
     * {@link com.github.podd.impl.PoddRepositoryManagerImpl#updateManagedPoddArtifactVersion(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID, boolean)}
     * .
     */
    @Ignore
    @Test
    public final void testUpdateManagedPoddArtifactVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
}
