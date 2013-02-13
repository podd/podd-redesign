package com.github.podd.ontology.test;

import java.io.IOException;
import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.exception.PoddException;
import com.github.podd.impl.PoddArtifactManagerImpl;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.impl.file.PoddFileReferenceManagerImpl;
import com.github.podd.impl.purl.PoddPurlManagerImpl;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Abstract test class for testing PODD artifacts and schema ontologies in a Sesame repository.
 * 
 * @author kutila
 * 
 */
public class AbstractOntologyTest
{
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Repository testRepository;
    
    private PoddRepositoryManager poddRepositoryManager;
    
    private PoddSchemaManager poddSchemaManager;
    
    private PoddArtifactManager poddArtifactManager;
    
    protected URI schemaGraph = ValueFactoryImpl.getInstance().createURI("urn:SCHEMA:Mgt");
    
    protected URI artifactGraph = ValueFactoryImpl.getInstance().createURI("urn:ARTIFACT:Mgt");
    
    /**
     * @return an initialized RepositoryConnection
     * @throws OpenRDFException
     */
    public RepositoryConnection getConnection() throws OpenRDFException
    {
        RepositoryConnection conn;
        conn = this.poddRepositoryManager.getRepository().getConnection();
        conn.begin();
        return conn;
    }
    
    /**
     * Initialize the test Repository by loading the schema ontologies and updating the management
     * graphs.
     * 
     * This is carried out using the PODD API Managers to ensure that the repository state is
     * consistent with what PODD would create.
     */
    private void initializeTestRepository()
    {
        final PoddFileReferenceProcessorFactoryRegistry nextFileRegistry =
                new PoddFileReferenceProcessorFactoryRegistry();
        // clear any automatically added entries that may come from META-INF/services entries on the
        // classpath
        nextFileRegistry.clear();
        
        final PoddPurlProcessorFactoryRegistry nextPurlRegistry = new PoddPurlProcessorFactoryRegistry();
        nextPurlRegistry.clear();
        final PoddPurlProcessorFactory nextPurlProcessorFactory = new UUIDPurlProcessorFactoryImpl();
        nextPurlRegistry.add(nextPurlProcessorFactory);
        
        final PoddFileReferenceManager nextFileReferenceManager = new PoddFileReferenceManagerImpl();
        nextFileReferenceManager.setProcessorFactoryRegistry(nextFileRegistry);
        
        final PoddPurlManager nextPurlManager = new PoddPurlManagerImpl();
        nextPurlManager.setPurlProcessorFactoryRegistry(nextPurlRegistry);
        
        final PoddOWLManager nextOWLManager = new PoddOWLManagerImpl();
        nextOWLManager.setReasonerFactory(OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet"));
        final OWLOntologyManager nextOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        if(nextOWLOntologyManager == null)
        {
            this.log.error("OWLOntologyManager was null");
        }
        nextOWLManager.setOWLOntologyManager(nextOWLOntologyManager);
        
        this.poddRepositoryManager = new PoddRepositoryManagerImpl(this.testRepository);
        this.poddRepositoryManager.setSchemaManagementGraph(this.schemaGraph);
        this.poddRepositoryManager.setArtifactManagementGraph(this.artifactGraph);
        
        this.poddSchemaManager = new PoddSchemaManagerImpl();
        this.poddSchemaManager.setOwlManager(nextOWLManager);
        this.poddSchemaManager.setRepositoryManager(this.poddRepositoryManager);
        
        final PoddSesameManager poddSesameManager = new PoddSesameManagerImpl();
        
        this.poddArtifactManager = new PoddArtifactManagerImpl();
        this.poddArtifactManager.setRepositoryManager(this.poddRepositoryManager);
        this.poddArtifactManager.setFileReferenceManager(nextFileReferenceManager);
        this.poddArtifactManager.setPurlManager(nextPurlManager);
        this.poddArtifactManager.setOwlManager(nextOWLManager);
        this.poddArtifactManager.setSchemaManager(this.poddSchemaManager);
        this.poddArtifactManager.setSesameManager(poddSesameManager);
        
        /*
         * Since the schema ontology upload feature is not yet supported, necessary schemas are
         * uploaded here at application starts up.
         */
        try
        {
            this.poddSchemaManager.uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_DCTERMS), RDFFormat.RDFXML);
            this.poddSchemaManager.uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_FOAF), RDFFormat.RDFXML);
            this.poddSchemaManager.uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_USER), RDFFormat.RDFXML);
            this.poddSchemaManager.uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_BASE), RDFFormat.RDFXML);
            this.poddSchemaManager.uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_SCIENCE), RDFFormat.RDFXML);
            this.poddSchemaManager.uploadSchemaOntology(
                    this.getClass().getResourceAsStream(PoddRdfConstants.PATH_PODD_PLANT), RDFFormat.RDFXML);
        }
        catch(IOException | OpenRDFException | OWLException | PoddException e)
        {
            this.log.error("Fatal Error!!! Could not load schema ontologies", e);
        }
    }
    
    /**
     * Load an artifact to the test PODD repository.
     * 
     * @param resourcePath
     * @param format
     * @return
     * @throws Exception
     */
    public InferredOWLOntologyID loadArtifact(final String resourcePath, final RDFFormat format) throws Exception
    {
        final InputStream resourceStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Resource was null", resourceStream);
        
        return this.poddArtifactManager.loadArtifact(resourceStream, format);
    }
    
    /**
     * Helper method prints the contents of the given context of the given Repository.
     */
    public void printContents(final RepositoryConnection conn, final URI context) throws Exception
    {
        final StringBuilder b = new StringBuilder();
        b.append("==================================================\r\n");
        b.append("Graph = " + context);
        b.append("\r\n\r\n");
        final org.openrdf.repository.RepositoryResult<Statement> repoResults =
                conn.getStatements(null, null, null, false, context);
        while(repoResults.hasNext())
        {
            final Statement stmt = repoResults.next();
            b.append("   {" + stmt.getSubject() + "}   <" + stmt.getPredicate() + ">  {" + stmt.getObject() + "}\r\n");
        }
        b.append("==================================================\r\n");
        System.out.println(b.toString());
    }
    
    /**
     * Helper method prints contexts/graphs available in the given Repository.
     */
    public void printContexts(final RepositoryConnection conn) throws Exception
    {
        final java.util.HashSet<String> contextSet = new java.util.HashSet<String>();
        
        final org.openrdf.repository.RepositoryResult<Statement> repoResults =
                conn.getStatements(null, null, null, true);
        while(repoResults.hasNext())
        {
            final Statement stmt = repoResults.next();
            contextSet.add(stmt.getContext().stringValue());
        }
        
        final StringBuilder b = new StringBuilder();
        
        b.append("==================================================\r\n");
        b.append("Contexts in Repository:  \r\n");
        for(final String context : contextSet)
        {
            b.append(context);
            b.append("\r\n");
        }
        b.append("==================================================\r\n");
        System.out.println(b.toString());
    }
    
    @Before
    public void setUp() throws Exception
    {
        // create a memory Repository for tests
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        // populate repository with schema ontologies & schema management graph
        this.initializeTestRepository();
    }
    
    @After
    public void tearDown() throws Exception
    {
        // clear all resources and shut down PODD
        if(this.testRepository != null)
        {
            try
            {
                this.testRepository.shutDown();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Test repository could not be shutdown", e);
            }
        }
        this.testRepository = null;
        
    }
    
}