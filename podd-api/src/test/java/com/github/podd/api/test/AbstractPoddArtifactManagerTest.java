/**
 * 
 */
package com.github.podd.api.test;

import java.io.InputStream;

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
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class AbstractPoddArtifactManagerTest
{
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    private String poddBaseResourcePath = "/ontologies/poddBase.owl";
    
    private String poddPlantResourcePath = "/ontologies/poddPlant.owl";
    
    private String poddScienceResourcePath = "/ontologies/poddScience.owl";
    
    private PoddArtifactManager testArtifactManager;
    private PoddRepositoryManager testRepositoryManager;
    private PoddSchemaManager testSchemaManager;
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of PoddArtifactManager
     * for each invocation.
     * 
     * @return A new empty instance of an implementation of PoddArtifactManager.
     */
    protected abstract PoddArtifactManager getNewArtifactManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * PoddPurlProcessorFactory that can process DOI references for each invocation.
     * 
     * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
     *         process DOI references.
     */
    protected abstract PoddPurlProcessorFactory getNewDoiPurlProcessorFactory();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * PoddFileReferenceManager.
     * 
     * @return A new empty instance of an implementation of PoddFileReferenceManager.
     */
    protected abstract PoddFileReferenceManager getNewFileReferenceManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * PoddPurlProcessorFactory that can process Handle references for each invocation.
     * 
     * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
     *         process Handle references.
     */
    protected abstract PoddPurlProcessorFactory getNewHandlePurlProcessorFactory();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * PoddFileReferenceProcessorFactory that can process HTTP-based file references for each
     * invocation.
     * 
     * @return A new empty instance of an implementation of PoddFileReferenceProcessorFactory that
     *         can process HTTP-based file references.
     */
    protected abstract PoddFileReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of {@link PoddOWLManager}
     * .
     * 
     * @return A new empty instance of an implementation of PoddOWLManager.
     */
    protected abstract PoddOWLManager getNewOWLManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link PoddPurlManager}.
     * 
     * @return A new empty instance of an implementation of PoddPurlManager.
     */
    protected abstract PoddPurlManager getNewPurlManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link OWLReasonerFactory} that can be used with the {@link PoddOWLManager}.
     * 
     * @return A new empty instance of an implementation of OWLReasonerFactory.
     */
    protected abstract OWLReasonerFactory getNewReasonerFactory();
    
    /**
     * Concrete tests must override this to provide a new, initialised, instance of
     * {@link PoddRepositoryManager} with the desired {@link Repository} for this test.
     * 
     * @return A new, initialised. instance of {@link PoddRepositoryManager}
     * @throws OpenRDFException
     *             If there were problems creating or initialising the Repository.
     */
    protected abstract PoddRepositoryManager getNewRepositoryManager() throws OpenRDFException;
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link PoddSchemaManager}.
     * 
     * @return A new empty instance of an implementation of PoddSchemaManager.
     */
    protected abstract PoddSchemaManager getNewSchemaManager();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link PoddFileReferenceProcessorFactory} that can process SSH-based file references for each
     * invocation.
     * 
     * @return A new empty instance of an implementation of PoddFileReferenceProcessorFactory that
     *         can process SSH-based file references.
     */
    protected abstract PoddFileReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link PoddPurlProcessorFactory} that can process UUID references for each invocation.
     * 
     * @return A new empty instance of an implementation of PoddPurlProcessorFactory that can
     *         process UUID references.
     */
    protected abstract PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory();
    
    /**
     * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
     * 
     * @param resourcePath
     * @param format
     * @param assertedStatementCount
     * @param inferredStatementCount
     * @return
     * @throws Exception
     */
    private InferredOWLOntologyID loadInferStoreOntology(final String resourcePath, final RDFFormat format,
            final long assertedStatementCount, final long inferredStatementCount) throws Exception
    {
        // load ontology to OWLManager
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        final OWLOntologyDocumentSource owlSource =
                new StreamDocumentSource(inputStream, OWLOntologyFormatFactoryRegistry.getInstance().getByMIMEType(
                        format.getDefaultMIMEType()));
        
        final OWLOntology loadedBaseOntology = this.testArtifactManager.getOWLManager().loadOntology(owlSource);
        
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            this.testArtifactManager.getOWLManager().dumpOntologyToRepository(loadedBaseOntology,
                    nextRepositoryConnection);
            
            // infer statements and dump to repository
            final InferredOWLOntologyID inferredOntologyID =
                    this.testArtifactManager.getOWLManager().inferStatements(loadedBaseOntology,
                            nextRepositoryConnection);
            
            // verify statement counts
            final URI versionURI = loadedBaseOntology.getOntologyID().getVersionIRI().toOpenRDFURI();
            Assert.assertEquals("Wrong statement count", assertedStatementCount,
                    nextRepositoryConnection.size(versionURI));
            
            final URI inferredOntologyURI = inferredOntologyID.getInferredOntologyIRI().toOpenRDFURI();
            Assert.assertEquals("Wrong inferred statement count", inferredStatementCount,
                    nextRepositoryConnection.size(inferredOntologyURI));
            
            nextRepositoryConnection.commit();
            
            return inferredOntologyID;
        }
        catch(final Exception e)
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            throw e;
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
        }
    }
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // FIXME: This needs to be a constant
        final URI poddFileReferenceType =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#PoddFileReference");
        
        final PoddFileReferenceProcessorFactoryRegistry testFileRegistry =
                new PoddFileReferenceProcessorFactoryRegistry();
        // clear any automatically added entries that may come from META-INF/services entries on the
        // classpath
        testFileRegistry.clear();
        
        final PoddPurlProcessorFactoryRegistry testPurlRegistry = new PoddPurlProcessorFactoryRegistry();
        testPurlRegistry.clear();
        final PoddPurlProcessorFactory uuidFactory = this.getNewUUIDPurlProcessorFactory();
        Assert.assertNotNull("UUID factory was null", uuidFactory);
        testPurlRegistry.add(uuidFactory);
        
        /**
         * // In practice, the following factories would be automatically added to the registry, //
         * however for testing we want to explicitly add the ones we want to support for each test
         * PoddFileReferenceProcessorFactory sshFactory =
         * this.getNewSSHFileReferenceProcessorFactory();
         * Assert.assertNotNull("SSH factory was null", sshFactory);
         * testFileRegistry.add(sshFactory);
         * 
         * PoddFileReferenceProcessorFactory httpFactory =
         * this.getNewHttpFileReferenceProcessorFactory();
         * Assert.assertNotNull("HTTP factory was null", httpFactory);
         * testFileRegistry.add(httpFactory);
         */
        
        final PoddFileReferenceManager testFileReferenceManager = this.getNewFileReferenceManager();
        testFileReferenceManager.setProcessorFactoryRegistry(testFileRegistry);
        
        /**
         * // TODO: Implement these purl processor factories PoddPurlProcessorFactory doiFactory =
         * this.getNewDoiPurlProcessorFactory(); testPurlRegistry.add(doiFactory);
         * Assert.assertNotNull("DOI factory was null", httpFactory);
         * 
         * PoddPurlProcessorFactory handleFactory = this.getNewHandlePurlProcessorFactory();
         * testPurlRegistry.add(handleFactory); Assert.assertNotNull("Handle factory was null",
         * handleFactory);
         **/
        
        final PoddPurlManager testPurlManager = this.getNewPurlManager();
        testPurlManager.setPurlProcessorFactoryRegistry(testPurlRegistry);
        
        final PoddOWLManager testOWLManager = this.getNewOWLManager();
        testOWLManager.setReasonerFactory(this.getNewReasonerFactory());
        final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        Assert.assertNotNull("Null implementation of OWLOntologymanager", manager);
        testOWLManager.setOWLOntologyManager(manager);
        
        this.testRepositoryManager = this.getNewRepositoryManager();
        
        this.testSchemaManager = this.getNewSchemaManager();
        this.testSchemaManager.setOwlManager(testOWLManager);
        this.testSchemaManager.setRepositoryManager(this.testRepositoryManager);
        
        this.testArtifactManager = this.getNewArtifactManager();
        this.testArtifactManager.setRepositoryManager(this.testRepositoryManager);
        this.testArtifactManager.setFileReferenceManager(testFileReferenceManager);
        this.testArtifactManager.setPurlManager(testPurlManager);
        this.testArtifactManager.setOwlManager(testOWLManager);
        this.testArtifactManager.setSchemaManager(this.testSchemaManager);
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testArtifactManager = null;
    }
    
    @Test
    public final void testGetFileReferenceManager() throws Exception
    {
        Assert.assertNotNull("File Reference Manager was null", this.testArtifactManager.getFileReferenceManager());
    }
    
    @Test
    public final void testGetOWLManager() throws Exception
    {
        Assert.assertNotNull("OWL Manager was null", this.testArtifactManager.getOWLManager());
    }
    
    @Test
    public final void testGetPurlManager() throws Exception
    {
        Assert.assertNotNull("Purl Manager was null", this.testArtifactManager.getPurlManager());
    }
    
    @Test
    public final void testGetRepositoryManager() throws Exception
    {
        Assert.assertNotNull("Repository Manager was null", this.testArtifactManager.getRepositoryManager());
    }
    
    @Test
    public final void testGetSchemaManager() throws Exception
    {
        Assert.assertNotNull("Schema Manager was null", this.testArtifactManager.getSchemaManager());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testLoadArtifactBasicSuccess() throws Exception
    {
        // prepare: load schema ontologies
        final InferredOWLOntologyID inferredPBaseOntologyID =
                this.loadInferStoreOntology(this.poddBaseResourcePath, RDFFormat.RDFXML, 282, 114);
        final InferredOWLOntologyID inferredPScienceOntologyID =
                this.loadInferStoreOntology(this.poddScienceResourcePath, RDFFormat.RDFXML, 1588, 363);
        
        // prepare: update schema management graph
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(
                inferredPBaseOntologyID.getBaseOWLOntologyID(), inferredPBaseOntologyID.getInferredOWLOntologyID(),
                false);
        this.testRepositoryManager.updateCurrentManagedSchemaOntologyVersion(
                inferredPScienceOntologyID.getBaseOWLOntologyID(),
                inferredPScienceOntologyID.getInferredOWLOntologyID(), false);
        
        final InputStream inputStream =
                this.getClass().getResourceAsStream("/test/artifacts/basicProject-1-internal-object.rdf");
        // MIME type should be either given by the user, detected from the content type on the
        // request, or autodetected using the Any23 Mime Detector
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        // invoke test method
        final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
        
        // verify:
        Assert.assertNotNull("Load artifact returned a null artifact ID", resultArtifactId);
        Assert.assertNotNull("Load artifact returned a null ontology IRI", resultArtifactId.getOntologyIRI());
        Assert.assertNotNull("Load artifact returned a null ontology version IRI", resultArtifactId.getVersionIRI());
        Assert.assertNotNull("Load artifact returned a null inferred ontology IRI",
                resultArtifactId.getInferredOntologyIRI());
        
        // verify: based on size of graphs
        RepositoryConnection nextRepositoryConnection = null;
        try
        {
            nextRepositoryConnection = this.testRepositoryManager.getRepository().getConnection();
            nextRepositoryConnection.begin();
            
            Assert.assertEquals("Incorrect number of asserted statements for artifact", 33,
                    nextRepositoryConnection.size(resultArtifactId.getVersionIRI().toOpenRDFURI()));
            
            Assert.assertEquals("Incorrect number of inferred statements for artifact", 383,
                    nextRepositoryConnection.size(resultArtifactId.getInferredOntologyIRI().toOpenRDFURI()));
            
            Assert.assertEquals("Artifact graph not of expected size", 6,
                    nextRepositoryConnection.size(this.testRepositoryManager.getArtifactManagementGraph()));
        }
        finally
        {
            if(nextRepositoryConnection != null && nextRepositoryConnection.isActive())
            {
                nextRepositoryConnection.rollback();
            }
            if(nextRepositoryConnection != null && nextRepositoryConnection.isOpen())
            {
                nextRepositoryConnection.close();
            }
        }
        
        // FIXME: further verification based on content in repository
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testLoadArtifactWithEmptyOntology() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testLoadArtifactWithIncorrectFormat() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testLoadArtifactWithMissingSchemaOntologiesInRepository() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testLoadArtifactWithInconsistency() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testLoadArtifactWithTwoArtifacts() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testLoadArtifactWithTwoVersionsOfSameArtifact() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testPublishArtifact() throws Exception
    {
        final InputStream inputStream =
                this.getClass().getResourceAsStream("/test/artifacts/basicProject-1-internal-object.rdf");
        // MIME type should be either given by the user, detected from the content type on the
        // request, or autodetected using the Any23 Mime Detector
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        final InferredOWLOntologyID resultArtifactId = this.testArtifactManager.loadArtifact(inputStream, format);
        
        Assert.assertNotNull("Load artifact returned a null artifact ID", resultArtifactId);
        Assert.assertNotNull("Load artifact returned a null ontology IRI", resultArtifactId.getOntologyIRI());
        Assert.assertNotNull("Load artifact returned a null ontology version IRI", resultArtifactId.getVersionIRI());
        Assert.assertNotNull("Load artifact returned a null inferred ontology IRI",
                resultArtifactId.getInferredOntologyIRI());
        
        this.testArtifactManager.publishArtifact(resultArtifactId);
        
        // FIXME: How do we get information about whether an artifact is published and other
        // metadata like who can access the artifact?
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImport(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Test
    public final void testUpdateSchemaImport() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Helper method prints the contents of the given context of a Repository
     */
    private void printContents(final URI context) throws Exception
    {
        final RepositoryConnection conn = this.testRepositoryManager.getRepository().getConnection();
        conn.begin();
        
        System.out.println("==================================================");
        System.out.println("Graph = " + context);
        System.out.println();
        final org.openrdf.repository.RepositoryResult<Statement> repoResults =
                conn.getStatements(null, null, null, false, context);
        while(repoResults.hasNext())
        {
            final Statement stmt = repoResults.next();
            System.out.println("   {" + stmt.getSubject() + "}   <" + stmt.getPredicate() + ">  {" + stmt.getObject()
                    + "}");
        }
        System.out.println("==================================================");
        
        conn.rollback();
        conn.close();
    }
    
}
