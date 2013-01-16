/**
 * 
 */
package com.github.podd.restlet;

import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.impl.PoddArtifactManagerImpl;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.impl.file.PoddFileReferenceManagerImpl;
import com.github.podd.impl.purl.PoddPurlManagerImpl;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.PoddWebConstants;

/**
 * @author kutila
 * 
 */
public class PoddApiUtils
{
    public static final URI ARTIFACT_MGT_GRAPH = ValueFactoryImpl.getInstance().createURI("urn:test:artifact-graph");
    
    public static final URI SCHEMA_MGT_GRAPH = ValueFactoryImpl.getInstance().createURI("urn:test:schema-graph");
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    private Repository nextRepository;
    
    private PoddRepositoryManagerImpl poddRepositoryManager;
    private PoddSchemaManagerImpl poddSchemaManager;
    private PoddSesameManagerImpl poddSesameManager;
    private PoddArtifactManagerImpl poddArtifactManager;
    
    public void setUp()
    {
        final PoddFileReferenceProcessorFactoryRegistry testFileRegistry =
                new PoddFileReferenceProcessorFactoryRegistry();
        // clear any automatically added entries that may come from META-INF/services entries on the
        // classpath
        testFileRegistry.clear();
        
        final PoddPurlProcessorFactoryRegistry testPurlRegistry = new PoddPurlProcessorFactoryRegistry();
        testPurlRegistry.clear();
        final PoddPurlProcessorFactory uuidFactory = new UUIDPurlProcessorFactoryImpl();
        testPurlRegistry.add(uuidFactory);
        
        final PoddFileReferenceManager testFileReferenceManager = new PoddFileReferenceManagerImpl();
        testFileReferenceManager.setProcessorFactoryRegistry(testFileRegistry);
        
        final PoddPurlManager testPurlManager = new PoddPurlManagerImpl();
        testPurlManager.setPurlProcessorFactoryRegistry(testPurlRegistry);
        
        final PoddOWLManager testOWLManager = new PoddOWLManagerImpl();
        testOWLManager.setReasonerFactory(OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet"));
        final OWLOntologyManager manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        if(manager == null)
        {
            this.log.error("OWLOntologyManager was null");
        }
        testOWLManager.setOWLOntologyManager(manager);
        
        this.poddRepositoryManager = new PoddRepositoryManagerImpl(this.nextRepository);
        this.poddRepositoryManager.setSchemaManagementGraph(PoddApiUtils.SCHEMA_MGT_GRAPH);
        this.poddRepositoryManager.setArtifactManagementGraph(PoddApiUtils.ARTIFACT_MGT_GRAPH);
        
        this.poddSchemaManager = new PoddSchemaManagerImpl();
        this.poddSchemaManager.setOwlManager(testOWLManager);
        this.poddSchemaManager.setRepositoryManager(this.poddRepositoryManager);
        
        this.poddSesameManager = new PoddSesameManagerImpl();
        
        this.poddArtifactManager = new PoddArtifactManagerImpl();
        this.poddArtifactManager.setRepositoryManager(this.poddRepositoryManager);
        this.poddArtifactManager.setFileReferenceManager(testFileReferenceManager);
        this.poddArtifactManager.setPurlManager(testPurlManager);
        this.poddArtifactManager.setOwlManager(testOWLManager);
        this.poddArtifactManager.setSchemaManager(this.poddSchemaManager);
        this.poddArtifactManager.setSesameManager(this.poddSesameManager);
        
    }
    
    public void tearDown()
    {
        // clear all resources and shut down PODD
        if(this.nextRepository != null)
        {
            try
            {
                this.nextRepository.shutDown();
            }
            catch(final RepositoryException e)
            {
                this.log.error("Test repository could not be shutdown", e);
            }
        }
        this.nextRepository = null;
    }
    
    /**
     * The implementation does not yet support uploading of new Schema Ontologies. Therefore, this
     * method should be called at initialization to load the schemas.
     * 
     * @throws IOException
     * @throws OpenRDFException
     * @throws OWLException
     * 
     */
    public void loadSchemaOntologies() throws OWLException, OpenRDFException, IOException
    {
        this.log.info("loadSchemaOntologies ... start");
        
        final List<Entry<URI, String>> schemaOntologyList = new ArrayList<>();
        schemaOntologyList.add(new SimpleEntry<URI, String>(this.nextRepository.getValueFactory().createURI(
                PoddWebConstants.URI_PODD_BASE), PoddWebConstants.PATH_PODD_BASE));
        schemaOntologyList.add(new SimpleEntry<URI, String>(this.nextRepository.getValueFactory().createURI(
                PoddWebConstants.URI_PODD_SCIENCE), PoddWebConstants.PATH_PODD_SCIENCE));
        schemaOntologyList.add(new SimpleEntry<URI, String>(this.nextRepository.getValueFactory().createURI(
                PoddWebConstants.URI_PODD_PLANT), PoddWebConstants.PATH_PODD_PLANT));
        
        RepositoryConnection repositoryConnection = null;
        
        final List<URI> loadedSchemas = new ArrayList<>();
        
        try
        {
            repositoryConnection = this.nextRepository.getConnection();
            repositoryConnection.begin();
            
            for(final Entry<URI, String> schemaOntology : schemaOntologyList)
            {
                final RepositoryResult<Statement> repoResult =
                        repositoryConnection.getStatements(schemaOntology.getKey(),
                                PoddRdfConstants.OMV_CURRENT_VERSION, null, false, PoddApiUtils.SCHEMA_MGT_GRAPH);
                if(repoResult.hasNext())
                {
                    final Value object = repoResult.next().getObject();
                    if(object instanceof Resource)
                    {
                        this.log.info("loadSchemaOntology ... {} (from Repository)", object);
                        
                        // FIXME: load schema ontology
                        // this.utils.loadOntology(repositoryConnection,
                        // RDFFormat.RDFXML.getDefaultMIMEType(),
                        // (Resource)object);
                        loadedSchemas.add(schemaOntology.getKey());
                    }
                }
            }
            
            // load remaining schema ontologies from classpath resources
            for(final Entry<URI, String> schemaOntology : schemaOntologyList)
            {
                if(!loadedSchemas.contains(schemaOntology.getKey()))
                {
                    this.log.info("loadSchemaOntology ... {} (from Classpath) ({})", schemaOntology.getKey(),
                            repositoryConnection.size());
                    // FIXME: load schema ontology
                    // this.utils.loadInferAndStoreSchemaOntology(schemaOntology.getValue(),
                    // RDFFormat.RDFXML.getDefaultMIMEType(), repositoryConnection);
                }
            }
            
            this.log.info("loadSchemaOntology ... completed ({})", repositoryConnection.size());
            repositoryConnection.commit();
        }
        catch(final OpenRDFException e)
        {
            if(repositoryConnection != null)
            {
                repositoryConnection.rollback();
            }
            throw e;
        }
        finally
        {
            if(repositoryConnection != null)
            {
                try
                {
                    repositoryConnection.close();
                }
                catch(final RepositoryException e)
                {
                    this.log.error("Repository connection could not be closed", e);
                }
            }
        }
    }
    
    public void setRepository(final Repository repository)
    {
        this.nextRepository = repository;
    }
    
    public PoddRepositoryManagerImpl getPoddRepositoryManager()
    {
        return this.poddRepositoryManager;
    }
    
    public PoddSchemaManagerImpl getPoddSchemaManager()
    {
        return this.poddSchemaManager;
    }
    
    public PoddSesameManagerImpl getPoddSesameManager()
    {
        return this.poddSesameManager;
    }
    
    public PoddArtifactManagerImpl getPoddArtifactManager()
    {
        return this.poddArtifactManager;
    }
    
}
