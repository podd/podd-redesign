/*
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
 /**
 * 
 */
package com.github.podd.impl.purl;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.exception.PurlProcessorNotHandledException;
import com.github.podd.utils.PoddRdfUtils;

/**
 * Basic PURL Manager implementation for use in PODD.
 * 
 * 
 * @author kutila
 * 
 */
public class PoddPurlManagerImpl implements PoddPurlManager
{
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    // This manager functions only during the RDF_PARSING processor stage.
    private final PoddProcessorStage processorStage = PoddProcessorStage.RDF_PARSING;
    
    private PoddPurlProcessorFactoryRegistry purlProcessorFactoryRegistry;
    
    @Override
    public void convertTemporaryUris(final Set<PoddPurlReference> purlResults,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws RepositoryException,
        UpdateExecutionException
    {
        for(final PoddPurlReference purl : purlResults)
        {
            final URI inputUri = purl.getTemporaryURI();
            final URI outputUri = purl.getPurlURI();
            this.log.debug("Converting: {} to {}", inputUri, outputUri);
            
            this.convertTemporaryUri(inputUri, outputUri, repositoryConnection, contexts);
        }
    }
    
    private void convertTemporaryUri(final URI inputUri, final URI outputUri,
            final RepositoryConnection repositoryConnection, final URI... contexts) throws RepositoryException
    {
        // replace occurrences as Subject
        final RepositoryResult<Statement> stmtsWithTempSubject =
                repositoryConnection.getStatements(inputUri, null, null, false, contexts);
        try
        {
            while(stmtsWithTempSubject.hasNext())
            {
                final Statement next = stmtsWithTempSubject.next();
                repositoryConnection.add(outputUri, next.getPredicate(), next.getObject(), contexts);
                repositoryConnection.remove(next, contexts);
            }
        }
        finally
        {
            stmtsWithTempSubject.close();
        }
        
        // replace occurrences as Object
        final RepositoryResult<Statement> stmtsWithTempObject =
                repositoryConnection.getStatements(null, null, inputUri, false, contexts);
        try
        {
            while(stmtsWithTempObject.hasNext())
            {
                final Statement next = stmtsWithTempObject.next();
                repositoryConnection.add(next.getSubject(), next.getPredicate(), outputUri, contexts);
                repositoryConnection.remove(next, contexts);
            }
        }
        finally
        {
            stmtsWithTempObject.close();
        }
    }
    
    @Override
    public Set<PoddPurlReference> extractPurlReferences(final RepositoryConnection repositoryConnection,
            final URI... contexts) throws PurlProcessorNotHandledException, RepositoryException
    {
        return this.extractPurlReferences(null, repositoryConnection, contexts);
    }
    
    @Override
    public Set<PoddPurlReference> extractPurlReferences(final URI parentUri,
            final RepositoryConnection repositoryConnection, final URI... contexts)
        throws PurlProcessorNotHandledException, RepositoryException
    {
        final Set<PoddPurlReference> internalPurlResults =
                Collections.newSetFromMap(new ConcurrentHashMap<PoddPurlReference, Boolean>());
        // NOTE: We use a Set to avoid duplicate calls to any Purl processors for any
        // temporary URI
        final Set<URI> temporaryURIs = Collections.newSetFromMap(new ConcurrentHashMap<URI, Boolean>());
        
        // NOTE: a Factory may handle only a particular temporary URI format, necessitating to
        // go through multiple factories to extract ALL temporary URIs in the Repository.
        for(final PoddPurlProcessorFactory nextProcessorFactory : this.getPurlProcessorFactoryRegistry().getByStage(
                this.processorStage))
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
                
                // If the query matched anything, then for each of the temporary URIs in the
                // resulting construct statements, we create a Purl reference and add it to the
                // results
                while(queryResult.hasNext())
                {
                    final Statement next = queryResult.next();
                    // This processor factory matches the graph that we wish to use, so we create a
                    // processor instance now to create the PURL
                    // NOTE: This object cannot be shared as we do not specify that it needs to be
                    // thread safe
                    final PoddPurlProcessor processor = nextProcessorFactory.getProcessor();
                    
                    // Subject rewriting
                    if(next.getSubject() instanceof URI && !temporaryURIs.contains(next.getSubject())
                            && processor.canHandle((URI)next.getSubject()))
                    {
                        temporaryURIs.add((URI)next.getSubject());
                        internalPurlResults.add(processor.handleTranslation((URI)next.getSubject(), parentUri));
                    }
                    
                    // Predicate rewriting is not supported. Predicates in OWL Documents must
                    // be URIs from recognized vocabularies, so cannot be auto generated PURLs
                    
                    // Object rewriting
                    if(next.getObject() instanceof URI && !temporaryURIs.contains(next.getObject())
                            && processor.canHandle((URI)next.getObject()))
                    {
                        temporaryURIs.add((URI)next.getObject());
                        internalPurlResults.add(processor.handleTranslation((URI)next.getObject(), parentUri));
                    }
                }
            }
            catch(final MalformedQueryException | QueryEvaluationException e)
            {
                this.log.error("Unexpected query exception", e);
                // continue after logging an error, as another ProcessorFactory may generate a Purl
                // for this failed temporary URI
            }
        }
        return internalPurlResults;
    }
    
    @Override
    public PoddPurlProcessorFactoryRegistry getPurlProcessorFactoryRegistry()
    {
        return this.purlProcessorFactoryRegistry;
    }
    
    @Override
    public void setPurlProcessorFactoryRegistry(final PoddPurlProcessorFactoryRegistry purlProcessorFactoryRegistry)
    {
        this.purlProcessorFactoryRegistry = purlProcessorFactoryRegistry;
        
    }
    
}
