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

import info.aduna.iteration.Iterations;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.test.TestUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;

/**
 * Abstract test to verify that the PoddOWLManager API contract is followed by implementations.
 * 
 * TODO: add test cases for non-default cases (e.g. empty/null/invalid/non-matching values)
 * 
 * @author kutila
 * 
 */
public abstract class AbstractPoddOWLManagerTest
{
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();
    
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected PoddOWLManager testOwlManager;
    
    protected URI schemaGraph;
    
    protected PoddRepositoryManager testRepositoryManager;
    
    protected PoddSchemaManager testSchemaManager;
    
    protected PoddSesameManager testSesameManager;
    
    /**
     * @return A new OWLReasonerFactory instance for use with the PoddOWLManager
     */
    protected abstract OWLReasonerFactory getNewOWLReasonerFactoryInstance();
    
    /**
     * Concrete tests must override this to provide a new, empty, instance of
     * {@link OWLOntologyManagerFactory} that can be used with the {@link PoddOWLManager}.
     * 
     * @return A new empty instance of an implementation of {@link OWLOntologyManagerFactory}.
     */
    protected abstract OWLOntologyManagerFactory getNewOWLOntologyManagerFactory();
    
    /**
     * @return A new instance of PoddOWLManager, for each call to this method
     */
    protected abstract PoddOWLManager getNewPoddOWLManagerInstance(OWLOntologyManagerFactory nextManager,
            OWLReasonerFactory nextReasonerFactory);
    
    /**
     * 
     * @return A new instance of {@link PoddRepositoryManager}, for each call to this method.
     * @throws Exception
     */
    protected abstract PoddRepositoryManager getNewPoddRepositoryManagerInstance() throws Exception;
    
    /**
     * 
     * @return A new instance of {@link PoddSchemaManager}, for each call to this method.
     */
    protected abstract PoddSchemaManager getNewPoddSchemaManagerInstance();
    
    /**
     * 
     * @return A new instance of {@link PoddSesameManager}, for each call to this method.
     */
    protected abstract PoddSesameManager getNewPoddSesameManagerInstance();
    
    /**
     * Helper method which loads podd:dcTerms, podd:foaf and podd:User schema ontologies.
     */
    protected abstract List<InferredOWLOntologyID> loadDcFoafAndPoddUserSchemaOntologies() throws Exception;
    
    /**
     * Helper method which loads, infers and stores a given ontology using the PoddOWLManager.
     * 
     * @param resourcePath
     * @param format
     * @param assertedStatements
     * @param inferredStatements
     * @return
     * @throws Exception
     */
    protected abstract InferredOWLOntologyID loadInferStoreOntology(final String resourcePath, final RDFFormat format,
            final long assertedStatements, final long inferredStatements,
            final Set<? extends OWLOntologyID> dependentSchemaOntologies,
            final RepositoryConnection managementConnection) throws Exception;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        final OWLReasonerFactory reasonerFactory = this.getNewOWLReasonerFactoryInstance();
        Assert.assertNotNull("Null implementation of reasoner factory", reasonerFactory);
        
        final OWLOntologyManagerFactory managerFactory = this.getNewOWLOntologyManagerFactory();
        
        this.testOwlManager = this.getNewPoddOWLManagerInstance(managerFactory, reasonerFactory);
        Assert.assertNotNull("Null implementation of test OWLManager", this.testOwlManager);
        
        this.schemaGraph = PODD.VF.createURI("urn:test:owlmanager:schemagraph");
        
        this.testSchemaManager = this.getNewPoddSchemaManagerInstance();
        
        this.testRepositoryManager = this.getNewPoddRepositoryManagerInstance();
        this.testSchemaManager.setRepositoryManager(this.testRepositoryManager);
        
        this.testSesameManager = this.getNewPoddSesameManagerInstance();
        this.testSchemaManager.setSesameManager(this.testSesameManager);
        
        this.testSchemaManager.setOwlManager(this.testOwlManager);
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.schemaGraph = null;
        this.testSchemaManager = null;
        this.testSesameManager = null;
        this.testOwlManager = null;
        this.testRepositoryManager.shutDown();
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#removeCache(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testRemoveCacheWithNullOntology() throws Exception
    {
        try
        {
            this.testOwlManager.removeCache(null, null);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
        }
    }
    
    @Test
    public void testLoadAndInfer() throws Exception
    {
        RepositoryConnection managementConnection = this.testRepositoryManager.getManagementRepositoryConnection();
        try
        {
            OWLOntologyID replacementOntologyID = null;
            
            RioMemoryTripleSource owlSource = TestUtils.getRioTripleSource("/test/ontologies/version/1/a1.owl");
            
            InferredOWLOntologyID ontologyID =
                    this.testOwlManager.loadAndInfer(owlSource, managementConnection, replacementOntologyID,
                            Collections.<InferredOWLOntologyID> emptySet(), managementConnection, this.schemaGraph);
            
            Assert.assertNotNull(ontologyID);
            Assert.assertNotNull(ontologyID.getOntologyIRI());
            Assert.assertNotNull(ontologyID.getVersionIRI());
            Assert.assertNotNull(ontologyID.getInferredOntologyIRI());
        }
        finally
        {
            managementConnection.close();
        }
    }
}
