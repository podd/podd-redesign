/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.file.PoddFileReference;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;
import com.github.podd.utils.PoddRdfUtils;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddFileReferenceManagerImpl implements PoddFileReferenceManager
{
    
    private PoddFileReferenceProcessorFactoryRegistry registry;
    private PoddProcessorStage processorStage = PoddProcessorStage.RDF_PARSING;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 
     */
    public PoddFileReferenceManagerImpl()
    {
        // TODO Auto-generated constructor stub
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.PoddFileReferenceManager#addFileReference(com.github.podd.api.file
     * .PoddFileReference)
     */
    @Override
    public void addFileReference(PoddFileReference reference)
    {
        throw new RuntimeException("TODO: Implement addFileReference");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.PoddFileReferenceManager#deleteFileReference(com.github.podd.api
     * .file.PoddFileReference)
     */
    @Override
    public void deleteFileReference(PoddFileReference reference)
    {
        throw new RuntimeException("TODO: Implement deleteFileReference");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.PoddFileReferenceManager#extractFileReferences(org.openrdf.repository
     * .RepositoryConnection, org.openrdf.model.URI[])
     */
    @Override
    public Set<PoddFileReference> extractFileReferences(RepositoryConnection repositoryConnection, URI... contexts)
        throws RepositoryException
    {
        final Set<PoddFileReference> internalPurlResults =
                Collections.newSetFromMap(new ConcurrentHashMap<PoddFileReference, Boolean>());
        // NOTE: We use a Set to avoid duplicate calls to any Purl processors for any
        // temporary URI
        final Set<URI> temporaryURIs = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
        
        // NOTE: a Factory may handle only a particular temporary URI format, necessitating to
        // go through multiple factories to extract ALL temporary URIs in the Repository.
        for(final PoddFileReferenceProcessorFactory nextProcessorFactory : this.getFileProcessorFactoryRegistry()
                .getByStage(this.processorStage))
        {
            try
            {
                final String sparqlQuery = PoddRdfUtils.buildSparqlConstructQuery(nextProcessorFactory);
                
                final GraphQuery graphQuery = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery);
                
                // Create a new dataset to specify contexts that the query will be allowed to access
                final DatasetImpl dataset = new DatasetImpl();
                for(final URI artifactGraphUri : contexts)
                {
                    dataset.addDefaultGraph(artifactGraphUri);
                    dataset.addNamedGraph(artifactGraphUri);
                }
                
                // set the dataset for the query to be our artificially constructed dataset
                graphQuery.setDataset(dataset);
                
                final GraphQueryResult queryResult = graphQuery.evaluate();
                
                // FIXME: The following contains statements for the whole artifact
                Graph results = queryResult.asGraph();
                
                if(results != null)
                    throw new RuntimeException("TODO: Implement splitting graph into distinct file references");
                
                // This processor factory matches the graph that we wish to use, so we create a
                // processor instance now to create the PURL
                // NOTE: This object cannot be shared as we do not specify that it needs to be
                // threadsafe
                final PoddFileReferenceProcessor processor = nextProcessorFactory.getProcessor();
                
                if(processor.canHandle(results))
                {
                    internalPurlResults.add(processor.createReference(results));
                }
            }
            catch(final MalformedQueryException | QueryEvaluationException e)
            {
                this.log.error("Unexpected query exception", e);
            }
        }
        return internalPurlResults;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.PoddFileReferenceManager#getFileReferences(org.semanticweb.owlapi
     * .model.OWLOntologyID)
     */
    @Override
    public Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId)
    {
        throw new RuntimeException("TODO: Implement getFileReferences");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.PoddFileReferenceManager#getFileReferences(org.semanticweb.owlapi
     * .model.OWLOntologyID, org.semanticweb.owlapi.model.IRI)
     */
    @Override
    public Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId, IRI objectIri)
    {
        throw new RuntimeException("TODO: Implement getFileReferences");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.PoddFileReferenceManager#getFileReferences(org.semanticweb.owlapi
     * .model.OWLOntologyID, java.lang.String)
     */
    @Override
    public Set<PoddFileReference> getFileReferences(OWLOntologyID artifactId, String alias)
    {
        throw new RuntimeException("TODO: Implement getFileReferences");
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.file.PoddFileReferenceManager#getProcessorFactoryRegistry()
     */
    @Override
    public PoddFileReferenceProcessorFactoryRegistry getFileProcessorFactoryRegistry()
    {
        return this.registry;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.PoddFileReferenceManager#setProcessorFactoryRegistry(com.github.
     * podd.api.file.PoddFileReferenceProcessorFactoryRegistry)
     */
    @Override
    public void setProcessorFactoryRegistry(PoddFileReferenceProcessorFactoryRegistry registry)
    {
        this.registry = registry;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.file.PoddFileReferenceManager#verifyFileReferences(java.util.Set,
     * org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI)
     */
    @Override
    public void verifyFileReferences(Set<PoddFileReference> fileReferenceResults, RepositoryConnection tempConn,
            URI openRDFURI) throws RepositoryException
    {
        throw new RuntimeException("TODO: Implement verifyFileReferences");
    }
    
}
