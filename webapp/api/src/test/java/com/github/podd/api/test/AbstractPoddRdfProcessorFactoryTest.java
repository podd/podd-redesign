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
package com.github.podd.api.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.PoddProcessorFactory;
import com.github.podd.api.PoddRdfProcessor;
import com.github.podd.api.PoddRdfProcessorFactory;
import com.github.podd.utils.PoddRdfUtils;

/**
 * Abstract class to test PoddRdfProcessorFactory
 * 
 * @author kutila
 * 
 */
public abstract class AbstractPoddRdfProcessorFactoryTest<T extends PoddRdfProcessor> extends
        AbstractPoddProcessorFactoryTest<T, Model>
{
    
    /*
     * We keep a separate instance of PoddRDFProcessorFactory in addition to the PoddProcessFactory
     * instance available from the super-class.
     */
    private PoddRdfProcessorFactory<T> rdfProcessorFactory;
    
    @Override
    protected final PoddProcessorFactory<T, Model> getNewPoddProcessorFactory()
    {
        return this.getNewPoddRdfProcessorFactory();
    }
    
    /**
     * 
     * @return A new PodRdfProcessorFactory instance for use in this test
     */
    protected abstract PoddRdfProcessorFactory<T> getNewPoddRdfProcessorFactory();
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.rdfProcessorFactory = this.getNewPoddRdfProcessorFactory();
        Assert.assertNotNull("Null implementation of test processor factory", this.rdfProcessorFactory);
    }
    
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        this.rdfProcessorFactory = null;
    }
    
    @Test
    public void testGetSPARQLConstructBGP() throws Exception
    {
        Assert.assertNotNull("BGP was null", this.rdfProcessorFactory.getSPARQLConstructBGP());
    }
    
    @Test
    public void testGetSPARQLConstructWhere() throws Exception
    {
        Assert.assertNotNull("WHERE was null", this.rdfProcessorFactory.getSPARQLConstructWhere());
    }
    
    @Test
    public void testGetSPARQLGroupBy() throws Exception
    {
        Assert.assertNotNull("GROUP BY was null", this.rdfProcessorFactory.getSPARQLGroupBy());
    }
    
    @Test
    public void testGetSPARQLVariable() throws Exception
    {
        Assert.assertNotNull("SPARQL variable was null", this.rdfProcessorFactory.getSPARQLVariable());
    }
    
    /**
     * Test that a valid SPARQL query can be constructed based on the parts returned by this
     * factory.
     * 
     * @throws Exception
     */
    @Test
    public void testSPARQLQueryString() throws Exception
    {
        final String sparqlQuery = PoddRdfUtils.buildSparqlConstructQuery(this.rdfProcessorFactory);
        
        final Repository repository = new SailRepository(new MemoryStore());
        try
        {
            repository.initialize();
            final RepositoryConnection repositoryConnection = repository.getConnection();
            
            repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery);
            
            repositoryConnection.close();
        }
        finally
        {
            repository.shutDown();
        }
    }
    
    /**
     * Test that a valid SPARQL query can be constructed based on the parts returned by this
     * factory.
     * 
     * @throws Exception
     */
    @Test
    public void testSPARQLQueryStringWithSubject() throws Exception
    {
        final URI subject = ValueFactoryImpl.getInstance().createURI("http://example.com/podd/user#Will");
        
        final String sparqlQuery = PoddRdfUtils.buildSparqlConstructQuery(this.rdfProcessorFactory, subject);
        
        final Repository repository = new SailRepository(new MemoryStore());
        try
        {
            repository.initialize();
            final RepositoryConnection repositoryConnection = repository.getConnection();
            
            repositoryConnection.prepareGraphQuery(QueryLanguage.SPARQL, sparqlQuery);
            
            repositoryConnection.close();
        }
        finally
        {
            repository.shutDown();
        }
    }
    
}
