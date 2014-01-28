/**
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
package com.github.podd.impl.data.test;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
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

import com.github.podd.api.PoddRdfProcessorFactory;
import com.github.podd.api.data.DataReferenceProcessor;
import com.github.podd.api.data.SSHFileReference;
import com.github.podd.api.data.SSHFileReferenceProcessor;
import com.github.podd.api.test.AbstractPoddRdfProcessorFactoryTest;
import com.github.podd.api.test.TestConstants;
import com.github.podd.impl.data.SSHFileReferenceProcessorFactoryImpl;
import com.github.podd.utils.PODD;
import com.github.podd.utils.PoddRdfUtils;

/**
 * Concrete class to test SSHFileReferenceProcessorFactoryImpl. In addition to the abstract
 * super-classes tests that verify a valid SPARQL is generated, two tests which verify that they are
 * able to extract SSH File References from an RDF graph are included.
 * 
 * @author kutila
 */
public class SSHFileReferenceProcessorFactoryImplTest extends
        AbstractPoddRdfProcessorFactoryTest<DataReferenceProcessor<SSHFileReference>>
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
        final PoddRdfProcessorFactory<SSHFileReferenceProcessor> rdfProcessorFactory =
                this.getNewPoddRdfProcessorFactory();
        
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
            try (final InputStream inputStream = this.getClass().getResourceAsStream(artifactResourcePath);)
            {
                Assert.assertNotNull("Could not find resource", inputStream);
                repositoryConnection.add(inputStream, "", RDFFormat.RDFXML);
            }
            
            repositoryConnection.commit();
            repositoryConnection.begin();
            
            final GraphQuery query = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
            
            // verify SPARQL generated a graph as expected
            final Model model = QueryResults.asModel(query.evaluate());
            
            Assert.assertFalse("Empty Model, no file references found.", model.isEmpty());
            final Model type = model.filter(null, RDF.TYPE, PODD.PODD_BASE_DATA_REFERENCE_TYPE);
            Assert.assertEquals("Expected 2 file references", 2, type.size());
        }
        finally
        {
            repositoryConnection.rollback();
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
        final PoddRdfProcessorFactory<SSHFileReferenceProcessor> rdfProcessorFactory =
                this.getNewPoddRdfProcessorFactory();
        
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
            try (final InputStream inputStream = this.getClass().getResourceAsStream(artifactResourcePath);)
            {
                Assert.assertNotNull("Could not find resource", inputStream);
                repositoryConnection.add(inputStream, "", RDFFormat.RDFXML);
            }
            
            repositoryConnection.commit();
            repositoryConnection.begin();
            
            final GraphQuery query = repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparql);
            
            // verify SPARQL generated a graph as expected
            final Model model = QueryResults.asModel(query.evaluate());
            Assert.assertFalse("Empty Model, no file references found.", model.isEmpty());
            final Model type = model.filter(null, RDF.TYPE, PODD.PODD_BASE_DATA_REFERENCE_TYPE);
            Assert.assertEquals("Expected only 1 file reference", 1, type.size());
            Assert.assertEquals("Not the expected file reference", fileReference, type.subjects().iterator().next()
                    .stringValue());
        }
        finally
        {
            repositoryConnection.rollback();
            
            if(repositoryConnection != null)
            {
                repositoryConnection.close();
            }
            repository.shutDown();
        }
    }
    
}
