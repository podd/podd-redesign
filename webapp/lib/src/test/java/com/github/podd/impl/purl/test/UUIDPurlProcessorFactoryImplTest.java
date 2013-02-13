/**
 * 
 */
package com.github.podd.impl.purl.test;

import java.io.InputStream;

import junit.framework.Assert;

import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddRdfProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.PoddPurlProcessorPrefixes;
import com.github.podd.api.test.AbstractPoddRdfProcessorFactoryTest;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;
import com.github.podd.utils.PoddRdfUtils;

/**
 * Concrete unit test for UUIDPurlProcessorFactoryImpl
 * 
 * @author kutila
 * 
 */
public class UUIDPurlProcessorFactoryImplTest extends AbstractPoddRdfProcessorFactoryTest<PoddPurlProcessor>
{
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    protected PoddRdfProcessorFactory<PoddPurlProcessor> getNewPoddRdfProcessorFactory()
    {
        return new UUIDPurlProcessorFactoryImpl();
    }
    
    /**
     * Test that the SPARQL query which can be constructed based on the parts returned by this
     * factory can correctly execute against a test RDF graph.
     * 
     * @throws Exception
     */
    @Test
    public void testSPARQLQueryViaExecution() throws Exception
    {
        final PoddRdfProcessorFactory<PoddPurlProcessor> rdfProcessorFactory = this.getNewPoddRdfProcessorFactory();
        
        // build SPARQL query
        final String sparql = PoddRdfUtils.buildSparqlConstructQuery(rdfProcessorFactory);
        this.log.info("Generated SPARQL <{}> ", sparql);
        
        final Repository repository = new SailRepository(new MemoryStore());
        RepositoryConnection repositoryConnection = null;
        try
        {
            repository.initialize();
            repositoryConnection = repository.getConnection();
            repositoryConnection.begin();
            
            // load RDF graph into Repository
            final String artifactResourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
            final InputStream inputStream = this.getClass().getResourceAsStream(artifactResourcePath);
            Assert.assertNotNull("Could not find resource", inputStream);
            
            repositoryConnection.add(inputStream, "", RDFFormat.RDFXML);
            repositoryConnection.commit();
            repositoryConnection.begin();
            
            final GraphQuery query = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
            
            // verify SPARQL generated a graph as expected
            final GraphQueryResult result = query.evaluate();
            int count = 0;
            while(result.hasNext())
            {
                final Statement statement = result.next();
                count++;
                final boolean tempUriFound =
                        statement.getSubject().stringValue()
                                .startsWith(PoddPurlProcessorPrefixes.UUID.getTemporaryPrefix())
                                || statement.getObject().stringValue()
                                        .startsWith(PoddPurlProcessorPrefixes.UUID.getTemporaryPrefix());
                Assert.assertTrue("Generated graph contains statement without temporary URIs: ", tempUriFound);
            }
            Assert.assertEquals("SPARQL query did not match expected number of statements", 19, count);
            
            result.close();
            repositoryConnection.rollback();
        }
        finally
        {
            if(repositoryConnection != null)
            {
                repositoryConnection.close();
            }
            repository.shutDown();
        }
    }
    
    /**
     * Test that the SPARQL query which can be constructed based on the parts returned by this
     * factory, and a user specified "subject" can correctly execute against a test RDF graph.
     * 
     * @throws Exception
     */
    @Test
    public void testSPARQLQueryWithSubjectViaExecution() throws Exception
    {
        final PoddRdfProcessorFactory<PoddPurlProcessor> rdfProcessorFactory = this.getNewPoddRdfProcessorFactory();
        
        // build SPARQL query
        final URI subject = ValueFactoryImpl.getInstance().createURI("urn:temp:uuid:object:2966");
        final String sparql = PoddRdfUtils.buildSparqlConstructQuery(rdfProcessorFactory, subject);
        this.log.info("Generated SPARQL <{}> ", sparql);
        
        // verify SPARQL generated a graph as expected
        final Repository repository = new SailRepository(new MemoryStore());
        RepositoryConnection repositoryConnection = null;
        try
        {
            repository.initialize();
            repositoryConnection = repository.getConnection();
            repositoryConnection.begin();
            
            // load RDF graph into Repository
            final String artifactResourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
            final InputStream inputStream = this.getClass().getResourceAsStream(artifactResourcePath);
            Assert.assertNotNull("Could not find resource", inputStream);
            
            repositoryConnection.add(inputStream, "", RDFFormat.RDFXML);
            repositoryConnection.commit();
            repositoryConnection.begin();
            
            final GraphQuery query = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
            
            // verify SPARQL generated a graph as expected
            final GraphQueryResult result = query.evaluate();
            int count = 0;
            while(result.hasNext())
            {
                final Statement statement = result.next();
                Assert.assertEquals("Subject was not what was requested", subject, statement.getSubject());
                count++;
            }
            Assert.assertEquals("SPARQL query did not match expected number of statements", 13, count);
            result.close();
            repositoryConnection.rollback();
        }
        finally
        {
            if(repositoryConnection != null)
            {
                repositoryConnection.close();
            }
            repository.shutDown();
        }
    }
}
