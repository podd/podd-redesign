/**
 * 
 */
package com.github.podd.impl.file;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.file.DataReference;
import com.github.podd.api.file.DataReferenceManager;
import com.github.podd.api.file.DataReferenceProcessor;
import com.github.podd.api.file.DataReferenceProcessorFactory;
import com.github.podd.api.file.DataReferenceProcessorRegistry;
import com.github.podd.utils.PoddRdfUtils;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class FileReferenceManagerImpl implements DataReferenceManager
{
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    
    // Initially setup the registry to the global instance
    private DataReferenceProcessorRegistry registry = DataReferenceProcessorRegistry.getInstance();
    
    private final PoddProcessorStage processorStage = PoddProcessorStage.RDF_PARSING;
    
    /**
     * Default constructor
     */
    public FileReferenceManagerImpl()
    {
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.PoddFileReferenceManager#extractFileReferences(org.openrdf.repository
     * .RepositoryConnection, org.openrdf.model.URI[])
     */
    @Override
    public Set<DataReference> extractDataReferences(final RepositoryConnection repositoryConnection,
            final URI... contexts) throws OpenRDFException
    {
        final Set<DataReference> internalFileRefResults =
                Collections.newSetFromMap(new ConcurrentHashMap<DataReference, Boolean>());
        
        for(final DataReferenceProcessorFactory nextProcessorFactory : this.getDataProcessorRegistry()
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
                }
                
                // set the dataset for the query to be our artificially constructed dataset
                graphQuery.setDataset(dataset);
                
                final GraphQueryResult queryResult = graphQuery.evaluate();
                
                // following contains statements for file references from the whole artifact
                final Model results = QueryResults.asModel(queryResult);
                
                if(!results.isEmpty())
                {
                    // This processor factory matches the graph that we wish to use, so we create a
                    // processor instance now to create the File Reference
                    // NOTE: This object cannot be shared as we do not specify that it needs to be
                    // threadsafe
                    final DataReferenceProcessor<DataReference> processor = nextProcessorFactory.getProcessor();
                    
                    if(processor.canHandle(results))
                    {
                        internalFileRefResults.addAll(processor.createReferences(results));
                    }
                }
            }
            catch(final MalformedQueryException | QueryEvaluationException e)
            {
                this.log.error("Unexpected query exception", e);
            }
        }
        return internalFileRefResults;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.file.PoddFileReferenceManager#getProcessorFactoryRegistry()
     */
    @Override
    public DataReferenceProcessorRegistry getDataProcessorRegistry()
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
    public void setDataProcessorRegistry(final DataReferenceProcessorRegistry registry)
    {
        this.registry = registry;
    }
    
}
