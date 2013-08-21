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
package com.github.podd.prototype.test;

import java.util.Collection;
import java.util.Collections;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.prototype.URITranslator;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class URITranslatorTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(URITranslatorTest.class);
    
    private Repository testRepository;
    private ValueFactory testValueFactory;
    private RepositoryConnection testRepositoryConnection;
    
    private String testInputUriPrefix1;
    private String testOutputUriPrefix1;
    
    private URI testInputSubjectUri1;
    private URI testInputPredicateUri1;
    private URI testInputObjectUri1;
    
    private Collection<URI> testSubjectMappingPredicatesEmpty;
    private Collection<URI> testPredicateMappingPredicatesEmpty;
    private Collection<URI> testObjectMappingPredicatesEmpty;
    
    private boolean testDeleteTranslatedTriplesTrue;
    
    private Resource testContext1;
    
    private URI testOutputSubjectUri1;
    private URI testOutputPredicateUri1;
    private URI testOutputObjectUri1;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        this.testValueFactory = this.testRepository.getValueFactory();
        
        this.testRepositoryConnection = this.testRepository.getConnection();
        
        this.testDeleteTranslatedTriplesTrue = true;
        
        this.testInputUriPrefix1 = "urn:temp:";
        this.testOutputUriPrefix1 = "http://test.example.org/after/translation/";
        
        this.testInputSubjectUri1 = this.testValueFactory.createURI("urn:temp:testInputSubjectUri1");
        this.testOutputSubjectUri1 =
                this.testValueFactory.createURI("http://test.example.org/after/translation/testInputSubjectUri1");
        
        this.testInputPredicateUri1 = this.testValueFactory.createURI("urn:temp:testInputPredicateUri1");
        this.testOutputPredicateUri1 =
                this.testValueFactory.createURI("http://test.example.org/after/translation/testInputPredicateUri1");
        
        this.testInputObjectUri1 = this.testValueFactory.createURI("urn:temp:testInputObjectUri1");
        this.testOutputObjectUri1 =
                this.testValueFactory.createURI("http://test.example.org/after/translation/testInputObjectUri1");
        
        this.testSubjectMappingPredicatesEmpty = Collections.emptyList();
        this.testPredicateMappingPredicatesEmpty = Collections.emptyList();
        this.testObjectMappingPredicatesEmpty = Collections.emptyList();
        
        this.testContext1 = this.testValueFactory.createURI("urn:test:context:1");
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        if(this.testRepositoryConnection != null)
        {
            try
            {
                this.testRepositoryConnection.close();
            }
            catch(final RepositoryException e)
            {
                URITranslatorTest.LOGGER.error("Found unexpected exception while closing test repository connection");
            }
        }
        
        this.testValueFactory = null;
        
        if(this.testRepository != null)
        {
            try
            {
                this.testRepository.shutDown();
            }
            catch(final RepositoryException e)
            {
                URITranslatorTest.LOGGER.error("Repository did not shut down correctly in test tearDown", e);
            }
        }
        
        this.testRepository = null;
        this.testInputUriPrefix1 = null;
        this.testOutputUriPrefix1 = null;
        
        this.testInputSubjectUri1 = null;
        this.testOutputSubjectUri1 = null;
        this.testInputPredicateUri1 = null;
        this.testOutputPredicateUri1 = null;
        this.testInputObjectUri1 = null;
        this.testOutputObjectUri1 = null;
        
        this.testSubjectMappingPredicatesEmpty = null;
        this.testPredicateMappingPredicatesEmpty = null;
        this.testObjectMappingPredicatesEmpty = null;
        this.testDeleteTranslatedTriplesTrue = true;
        this.testContext1 = null;
    }
    
    /**
     * 
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingNoExactOneContext() throws RepositoryException, MalformedQueryException,
        UpdateExecutionException
    {
        this.testRepositoryConnection.add(this.testInputSubjectUri1, this.testInputPredicateUri1,
                this.testInputObjectUri1, this.testContext1);
        
        Assert.assertEquals(1, this.testRepositoryConnection.size());
        Assert.assertEquals(1, this.testRepositoryConnection.size(this.testContext1));
        
        for(final Statement nextBeforeStatement : this.testRepositoryConnection.getStatements(null, null, null, false,
                this.testContext1).asList())
        {
            URITranslatorTest.LOGGER.info("nextBeforeStatement: " + nextBeforeStatement.toString());
        }
        
        URITranslator.doTranslation(this.testRepositoryConnection, this.testInputUriPrefix1, this.testOutputUriPrefix1,
                this.testSubjectMappingPredicatesEmpty, true, false, this.testPredicateMappingPredicatesEmpty, true,
                false, this.testObjectMappingPredicatesEmpty, true, false, this.testDeleteTranslatedTriplesTrue,
                this.testContext1);
        
        for(final Statement nextAfterStatement : this.testRepositoryConnection.getStatements(null, null, null, false,
                this.testContext1).asList())
        {
            URITranslatorTest.LOGGER.info("nextAfterStatement: " + nextAfterStatement.toString());
        }
        
        Assert.assertEquals(1, this.testRepositoryConnection.size(this.testContext1));
        Assert.assertEquals(1, this.testRepositoryConnection.size());
        
        Assert.assertTrue(this.testRepositoryConnection.hasStatement(this.testOutputSubjectUri1,
                this.testOutputPredicateUri1, this.testOutputObjectUri1, false, this.testContext1));
    }
    
    /**
     * 
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingExactSubjectOnlyOneContext() throws RepositoryException,
        MalformedQueryException, UpdateExecutionException
    {
        this.testRepositoryConnection.add(this.testInputSubjectUri1, this.testInputPredicateUri1,
                this.testInputObjectUri1, this.testContext1);
        
        Assert.assertEquals(1, this.testRepositoryConnection.size());
        Assert.assertEquals(1, this.testRepositoryConnection.size(this.testContext1));
        
        for(final Statement nextBeforeStatement : this.testRepositoryConnection.getStatements(null, null, null, false,
                this.testContext1).asList())
        {
            URITranslatorTest.LOGGER.info("nextBeforeStatement: " + nextBeforeStatement.toString());
        }
        
        URITranslator.doTranslation(this.testRepositoryConnection, this.testInputSubjectUri1.stringValue(),
                this.testOutputSubjectUri1.stringValue(), this.testSubjectMappingPredicatesEmpty, true, true,
                this.testPredicateMappingPredicatesEmpty, true, true, this.testObjectMappingPredicatesEmpty, true,
                true, this.testDeleteTranslatedTriplesTrue, this.testContext1);
        
        for(final Statement nextAfterStatement : this.testRepositoryConnection.getStatements(null, null, null, false,
                this.testContext1).asList())
        {
            URITranslatorTest.LOGGER.info("nextAfterStatement: " + nextAfterStatement.toString());
        }
        
        Assert.assertEquals(1, this.testRepositoryConnection.size(this.testContext1));
        Assert.assertEquals(1, this.testRepositoryConnection.size());
        
        Assert.assertTrue(this.testRepositoryConnection.hasStatement(this.testOutputSubjectUri1,
                this.testInputPredicateUri1, this.testInputObjectUri1, false, this.testContext1));
    }
    
    /**
     * 
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingExactPredicateOnlyOneContext() throws RepositoryException,
        MalformedQueryException, UpdateExecutionException
    {
        this.testRepositoryConnection.add(this.testInputSubjectUri1, this.testInputPredicateUri1,
                this.testInputObjectUri1, this.testContext1);
        
        Assert.assertEquals(1, this.testRepositoryConnection.size());
        Assert.assertEquals(1, this.testRepositoryConnection.size(this.testContext1));
        
        for(final Statement nextBeforeStatement : this.testRepositoryConnection.getStatements(null, null, null, false,
                this.testContext1).asList())
        {
            URITranslatorTest.LOGGER.info("nextBeforeStatement: " + nextBeforeStatement.toString());
        }
        
        URITranslator.doTranslation(this.testRepositoryConnection, this.testInputPredicateUri1.stringValue(),
                this.testOutputPredicateUri1.stringValue(), this.testSubjectMappingPredicatesEmpty, true, true,
                this.testPredicateMappingPredicatesEmpty, true, true, this.testObjectMappingPredicatesEmpty, true,
                true, this.testDeleteTranslatedTriplesTrue, this.testContext1);
        
        for(final Statement nextAfterStatement : this.testRepositoryConnection.getStatements(null, null, null, false,
                this.testContext1).asList())
        {
            URITranslatorTest.LOGGER.info("nextAfterStatement: " + nextAfterStatement.toString());
        }
        
        Assert.assertEquals(1, this.testRepositoryConnection.size(this.testContext1));
        Assert.assertEquals(1, this.testRepositoryConnection.size());
        
        Assert.assertTrue(this.testRepositoryConnection.hasStatement(this.testInputSubjectUri1,
                this.testOutputPredicateUri1, this.testInputObjectUri1, false, this.testContext1));
    }
    
    /**
     * 
     * @throws UpdateExecutionException
     * @throws MalformedQueryException
     * @throws RepositoryException
     */
    @Test
    public void testDoTranslationNoMappingExactObjectOnlyOneContext() throws RepositoryException,
        MalformedQueryException, UpdateExecutionException
    {
        this.testRepositoryConnection.add(this.testInputSubjectUri1, this.testInputPredicateUri1,
                this.testInputObjectUri1, this.testContext1);
        
        Assert.assertEquals(1, this.testRepositoryConnection.size());
        Assert.assertEquals(1, this.testRepositoryConnection.size(this.testContext1));
        
        for(final Statement nextBeforeStatement : this.testRepositoryConnection.getStatements(null, null, null, false,
                this.testContext1).asList())
        {
            URITranslatorTest.LOGGER.info("nextBeforeStatement: " + nextBeforeStatement.toString());
        }
        
        URITranslator.doTranslation(this.testRepositoryConnection, this.testInputObjectUri1.stringValue(),
                this.testOutputObjectUri1.stringValue(), this.testSubjectMappingPredicatesEmpty, true, true,
                this.testPredicateMappingPredicatesEmpty, true, true, this.testObjectMappingPredicatesEmpty, true,
                true, this.testDeleteTranslatedTriplesTrue, this.testContext1);
        
        for(final Statement nextAfterStatement : this.testRepositoryConnection.getStatements(null, null, null, false,
                this.testContext1).asList())
        {
            URITranslatorTest.LOGGER.info("nextAfterStatement: " + nextAfterStatement.toString());
        }
        
        Assert.assertEquals(1, this.testRepositoryConnection.size(this.testContext1));
        Assert.assertEquals(1, this.testRepositoryConnection.size());
        
        Assert.assertTrue(this.testRepositoryConnection.hasStatement(this.testInputSubjectUri1,
                this.testInputPredicateUri1, this.testOutputObjectUri1, false, this.testContext1));
    }
}
