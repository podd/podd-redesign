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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.formats.OWLOntologyFormatFactoryRegistry;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.io.StreamDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactory;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.exception.EmptyOntologyException;
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
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected PoddOWLManager testOWLManager;
    
    protected URI schemaGraph;
    
    private Repository testRepository;
    
    protected RepositoryConnection testManagementConnection;
    
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
            final Set<? extends OWLOntologyID> dependentSchemaOntologies) throws Exception;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.schemaGraph = PODD.VF.createURI("urn:test:owlmanager:schemagraph");
        
        // this.manager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        // Assert.assertNotNull("Null implementation of OWLOntologymanager", this.manager);
        
        final OWLReasonerFactory reasonerFactory = this.getNewOWLReasonerFactoryInstance();
        Assert.assertNotNull("Null implementation of reasoner factory", reasonerFactory);
        
        final OWLOntologyManagerFactory managerFactory = this.getNewOWLOntologyManagerFactory();
        
        this.testOWLManager = this.getNewPoddOWLManagerInstance(managerFactory, reasonerFactory);
        Assert.assertNotNull("Null implementation of test OWLManager", this.testOWLManager);
        
        // create a memory Repository for tests
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        this.testManagementConnection = this.testRepository.getConnection();
        // this.testRepositoryConnection.begin();
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        // this.testRepositoryConnection.rollback();
        this.testManagementConnection.close();
        this.testRepository.shutDown();
        
        this.testOWLManager = null;
    }
    
}
