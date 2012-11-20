/**
 * 
 */
package com.github.podd.api.test;

import java.io.InputStream;
import java.util.Iterator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.PoddRdfProcessorEvent;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class AbstractPoddArtifactManagerTest
{
    protected abstract PoddArtifactManager getNewArtifactManager();
    
    protected abstract PoddRdfProcessorEvent getNewFileReferenceEvent(Graph graph, OWLOntologyID artifactId);
    
    protected abstract PoddFileReferenceManager getNewFileReferenceManager();
    
    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("null")
    @Before
    public void setUp() throws Exception
    {
        final PoddFileReferenceProcessorFactoryRegistry testRegistry = new PoddFileReferenceProcessorFactoryRegistry();
        
        final PoddFileReferenceManager testFileReferenceManager = this.getNewFileReferenceManager();
        final PoddArtifactManager testArtifactManager = this.getNewArtifactManager();
        testArtifactManager.setFileReferenceManager(testFileReferenceManager);
        
        final InputStream inputStream = this.getClass().getResourceAsStream("/testArtifact.rdf");
        final RDFFormat format = RDFFormat.RDFXML;
        testArtifactManager.loadArtifact(inputStream, format);
        // testArtifactManager needs to create artifactId
        final OWLOntologyID artifactId = null;
        
        for(final PoddFileReferenceProcessorFactory nextProcessor : testRegistry
                .getByStage(PoddProcessorStage.RDF_PARSING))
        {
            final StringBuilder sparqlQuery = new StringBuilder();
            sparqlQuery.append("CONSTRUCT { ");
            sparqlQuery.append(nextProcessor.getSPARQLConstructBGP());
            sparqlQuery.append(" } WHERE { ");
            sparqlQuery.append(nextProcessor.getSPARQLConstructWhere());
            sparqlQuery.append(" } ");
            if(!nextProcessor.getSPARQLGroupBy().isEmpty())
            {
                sparqlQuery.append(" GROUP BY ");
                sparqlQuery.append(nextProcessor.getSPARQLGroupBy());
            }
            
            final RepositoryConnection conn = null;
            
            final GraphQuery graphQuery = conn.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery.toString());
            
            // Create a new dataset to specify the contexts that the query will be allowed to access
            final DatasetImpl dataset = new DatasetImpl();
            final URI artifactGraphUri = null;
            dataset.addDefaultGraph(artifactGraphUri);
            dataset.addNamedGraph(artifactGraphUri);
            // if the stage is after inferencing, add the inferred graph URI as well
            final URI artifactInferredGraphUri = null;
            dataset.addDefaultGraph(artifactInferredGraphUri);
            dataset.addNamedGraph(artifactInferredGraphUri);
            // set the dataset for the query to be our artificially constructed dataset
            graphQuery.setDataset(dataset);
            
            final StatementCollector results = new StatementCollector();
            
            final GraphQueryResult queryResult = graphQuery.evaluate();
            
            // TODO: testArtifactManager needs to generate these events inside of loadArtifact
            final Graph graph = new GraphImpl();
            while(queryResult.hasNext())
            {
                graph.add(queryResult.next());
            }
            
            final PoddRdfProcessorEvent event = this.getNewFileReferenceEvent(graph, artifactId);
            
            final PoddFileReferenceProcessor processor = nextProcessor.getProcessor(event);
            processor.setFileReferenceManager(testFileReferenceManager);
            
            // handle the event after RDF parsing, not before it
            processor.handleAfter(event);
            
            final URI poddFileReferenceType =
                    conn.getValueFactory().createURI("http://purl.org/podd/ns/poddBase#PoddFileReference");
            final Iterator<Statement> match = graph.match(null, RDF.TYPE, poddFileReferenceType);
            
            while(match.hasNext())
            {
                final Statement nextStatement = match.next();
                
                // testFileReferenceManager.addFileReference(processor.)
            }
        }
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deregisterProcessor(com.github.podd.api.PoddProcessorFactory, com.github.podd.api.PoddProcessorStage)}
     * .
     */
    @Test
    public final void testDeregisterProcessor()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#getProcessors(com.github.podd.api.PoddProcessorStage)}
     * .
     */
    @Test
    public final void testGetProcessors()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testLoadArtifact()
    {
        
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Test
    public final void testPublishArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#registerProcessor(com.github.podd.api.PoddProcessorFactory, com.github.podd.api.PoddProcessorStage)}
     * .
     */
    @Test
    public final void testRegisterProcessor()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImport(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Test
    public final void testUpdateSchemaImport()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
}
