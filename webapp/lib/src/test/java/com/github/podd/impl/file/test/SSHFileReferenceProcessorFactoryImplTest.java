/**
 * 
 */
package com.github.podd.impl.file.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResults;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddRdfProcessor;
import com.github.podd.api.PoddRdfProcessorFactory;
import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddSSHFileReference;
import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.test.AbstractPoddRdfProcessorFactoryTest;
import com.github.podd.api.test.TestConstants;
import com.github.podd.impl.file.SSHFileReferenceProcessorFactoryImpl;
import com.github.podd.utils.PoddRdfUtils;

/**
 * @author kutila
 * 
 */
public class SSHFileReferenceProcessorFactoryImplTest<T extends PoddRdfProcessor> extends
        AbstractPoddRdfProcessorFactoryTest<PoddFileReferenceProcessor<PoddSSHFileReference>>
{
    
    protected final Logger log = LoggerFactory.getLogger(this.getClass());
    
    @Override
    protected PoddRdfProcessorFactory getNewPoddRdfProcessorFactory()
    {
        return new SSHFileReferenceProcessorFactoryImpl();
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
            final String artifactResourcePath = TestConstants.TEST_ARTIFACT_PURLS_2_FILE_REFS;
            final InputStream inputStream = this.getClass().getResourceAsStream(artifactResourcePath);
            Assert.assertNotNull("Could not find resource", inputStream);
            
            repositoryConnection.add(inputStream, "", RDFFormat.RDFXML);
            repositoryConnection.commit();
            repositoryConnection.begin();
            
            final GraphQuery query = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
            
            // verify SPARQL generated a graph as expected
            final Model model = QueryResults.asModel(query.evaluate());
            
            Assert.assertFalse("Empty Model, no file references found.", model.isEmpty());
            Assert.assertEquals("Expected 2 file references", 2, model.subjects().size());
            
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
        
        final String fileReference = "http://purl.org/podd-test/130326f/object-rice-scan-34343-a";
        
        // build SPARQL query
        final URI subject = ValueFactoryImpl.getInstance().createURI(fileReference);
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
            final String artifactResourcePath = TestConstants.TEST_ARTIFACT_PURLS_2_FILE_REFS;
            final InputStream inputStream = this.getClass().getResourceAsStream(artifactResourcePath);
            Assert.assertNotNull("Could not find resource", inputStream);
            
            repositoryConnection.add(inputStream, "", RDFFormat.RDFXML);
            repositoryConnection.commit();
            repositoryConnection.begin();
            
            final GraphQuery query = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
            
            // verify SPARQL generated a graph as expected
            final Model model = QueryResults.asModel(query.evaluate());
            Assert.assertFalse("Empty Model, no file references found.", model.isEmpty());
            Assert.assertEquals("Expected only 1 file reference", 1, model.subjects().size());
            Assert.assertEquals("Not the expected file reference", fileReference, model.subjects().iterator().next()
                    .stringValue());
            
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
