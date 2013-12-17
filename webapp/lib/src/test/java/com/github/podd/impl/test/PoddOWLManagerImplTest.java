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
package com.github.podd.impl.test;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.BNode;
import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.TreeModel;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.test.AbstractPoddOWLManagerTest;
import com.github.podd.api.test.TestConstants;
import com.github.podd.exception.EmptyOntologyException;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.utils.DebugUtils;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PODD;

/**
 * @author kutila
 * 
 */
public class PoddOWLManagerImplTest extends AbstractPoddOWLManagerTest
{
    private String reasonerName = "Pellet";
    
    @Override
    protected OWLReasonerFactory getNewOWLReasonerFactoryInstance()
    {
        return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(this.reasonerName);
    }
    
    @Override
    protected PoddOWLManagerImpl getNewPoddOWLManagerInstance(final OWLOntologyManager manager,
            final OWLReasonerFactory reasonerFactory)
    {
        return new PoddOWLManagerImpl(manager, reasonerFactory);
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
     * .
     * 
     */
    @Test
    public void testCreateReasonerWithNull() throws Exception
    {
        try
        {
            ((PoddOWLManagerImpl)this.testOWLManager).createReasoner(null);
            Assert.fail("Should have thrown a Runtime Exception");
        }
        catch(final RuntimeException e)
        {
            Assert.assertTrue("Exception not expected type", e instanceof NullPointerException);
            // this exception is thrown by the OWL API with a null message
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
     * .
     * 
     */
    @Test
    public void testCreateReasoner() throws Exception
    {
        // prepare: load an Ontology independently
        final InputStream inputStream = this.getClass().getResourceAsStream(PODD.PATH_PODD_DCTERMS_V1);
        Assert.assertNotNull("Could not find resource", inputStream);
        final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        final OWLOntology loadedOntology = testOWLOntologyManager.loadOntologyFromOntologyDocument(inputStream);
        
        final OWLReasoner reasoner = ((PoddOWLManagerImpl)this.testOWLManager).createReasoner(loadedOntology);
        
        // verify:
        Assert.assertNotNull("Created reasoner was NULL", reasoner);
        Assert.assertEquals(this.getNewOWLReasonerFactoryInstance().getReasonerName(), reasoner.getReasonerName());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#createReasoner(org.semanticweb.owlapi.model.OWLOntology)}
     * .
     * 
     */
    @Test
    public void testCreateReasonerFromEmptyOntology() throws Exception
    {
        // prepare: load an Ontology independently
        final OWLOntologyManager testOWLOntologyManager = OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
        final OWLOntology emptyOntology = testOWLOntologyManager.createOntology();
        
        final OWLReasoner reasoner = ((PoddOWLManagerImpl)this.testOWLManager).createReasoner(emptyOntology);
        
        // verify:
        Assert.assertNotNull("Created reasoner was NULL", reasoner);
        Assert.assertEquals(this.getNewOWLReasonerFactoryInstance().getReasonerName(), reasoner.getReasonerName());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#parseRDFStatements(org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)}
     * .
     * 
     */
    @Test
    public void testParseRDFStatements() throws Exception
    {
        this.loadDcFoafAndPoddUserSchemaOntologies();
        
        // prepare: load poddBase schema ontology into the test repository
        final InputStream inputStream = this.getClass().getResourceAsStream(PODD.PATH_PODD_BASE_V1);
        Assert.assertNotNull("Could not find resource", inputStream);
        
        final URI contextOriginal = ValueFactoryImpl.getInstance().createURI("urn:test:context:original:");
        
        final Model statementsOriginal = new TreeModel(Rio.parse(inputStream, "", RDFFormat.RDFXML, contextOriginal));
        
        Assert.assertEquals("Not the expected number of statements in Repository",
                TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE - 9, statementsOriginal.size());
        this.testRepositoryConnection.add(statementsOriginal);
        Assert.assertEquals("Not the expected number of statements in Repository",
                TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE - 9,
                this.testRepositoryConnection.size(contextOriginal));
        
        final OWLOntologyID loadedOntologyID =
                ((PoddOWLManagerImpl)this.testOWLManager).parseRDFStatements(this.testRepositoryConnection,
                        contextOriginal);
        
        // verify:
        Assert.assertNotNull("OntologyID was null", loadedOntologyID);
        Assert.assertEquals("<http://purl.org/podd/ns/poddBase>", loadedOntologyID.getOntologyIRI().toQuotedString());
        Assert.assertEquals("<http://purl.org/podd/ns/version/poddBase/1>", loadedOntologyID.getVersionIRI()
                .toQuotedString());
        
        final OWLOntology loadedOntology = this.manager.getOntology(loadedOntologyID);
        Assert.assertNotNull("Ontology not in memory", loadedOntology);
        
        final URI contextOwlapi = ValueFactoryImpl.getInstance().createURI("urn:test:context:owlapi:");
        this.testOWLManager.dumpOntologyToRepository(loadedOntology, this.testRepositoryConnection, contextOwlapi);
        
        // verify:
        Assert.assertEquals("Dumped statement count not expected value",
                TestConstants.EXPECTED_TRIPLE_COUNT_PODD_BASE_CONCRETE,
                this.testRepositoryConnection.size(contextOwlapi));
        
        final Model statementsOwlapi = new TreeModel();
        
        this.testRepositoryConnection.export(new StatementCollector(statementsOwlapi), contextOwlapi);
        
        System.out.println("------------");
        System.out.println("RDF statements");
        System.out.println("------------");
        
        final StringWriter originalWriter = new StringWriter();
        Rio.write(statementsOriginal, originalWriter, RDFFormat.NTRIPLES);
        
        System.out.println("------------");
        System.out.println("OWLAPI statements");
        System.out.println("------------");
        
        final StringWriter owlapiWriter = new StringWriter();
        Rio.write(statementsOwlapi, owlapiWriter, RDFFormat.NTRIPLES);
        
        System.out.println("------------");
        System.out.println("Mismatched statements");
        System.out.println("------------");
        
        final Set<URI> displayedPredicates = new HashSet<URI>();
        
        for(final Statement nextOwlapiStatement : statementsOwlapi)
        {
            if(!(nextOwlapiStatement.getSubject() instanceof BNode)
                    && !(nextOwlapiStatement.getObject() instanceof BNode))
            {
                if(!statementsOriginal.contains(nextOwlapiStatement.getSubject(), nextOwlapiStatement.getPredicate(),
                        nextOwlapiStatement.getObject()))
                {
                    System.out.println(nextOwlapiStatement);
                }
            }
            else
            {
                final Model originalFilter = statementsOriginal.filter(null, nextOwlapiStatement.getPredicate(), null);
                final Model owlapiFilter = statementsOwlapi.filter(null, nextOwlapiStatement.getPredicate(), null);
                
                if(originalFilter.size() != owlapiFilter.size())
                {
                    if(!displayedPredicates.contains(nextOwlapiStatement.getPredicate()))
                    {
                        displayedPredicates.add(nextOwlapiStatement.getPredicate());
                        System.out.println("Original statements for predicate");
                        DebugUtils.printContents(originalFilter);
                        System.out.println("OWLAPI statements for predicate");
                        DebugUtils.printContents(owlapiFilter);
                    }
                }
            }
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#parseRDFStatements(org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)}
     * .
     * 
     */
    @Test
    public void testParseRDFStatementsFromEmptyRepository() throws Exception
    {
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:test:context:");
        try
        {
            ((PoddOWLManagerImpl)this.testOWLManager).parseRDFStatements(this.testRepositoryConnection, context);
        }
        catch(final EmptyOntologyException e)
        {
            Assert.assertEquals("No statements to create an ontology", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#parseRDFStatements(org.openrdf.repository.RepositoryConnection, org.openrdf.model.URI...)}
     * . This test asserts that when the RepositoryConnection has just one statement in it, a
     * non-empty and anonymous Ontology is loaded to the memory.
     */
    @Test
    public void testParseRDFStatementsFromRepositoryWithOneStatement() throws Exception
    {
        // prepare: add a single statement to the Repository so that it is not empty
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:test:context:");
        
        this.testRepositoryConnection.add(ValueFactoryImpl.getInstance().createURI("urn:dummy:subject"), RDF.TYPE,
                ValueFactoryImpl.getInstance().createURI("urn:dummy:object"), context);
        
        final OWLOntologyID loadedOntologyID =
                ((PoddOWLManagerImpl)this.testOWLManager).parseRDFStatements(this.testRepositoryConnection, context);
        
        // verify:
        Assert.assertNotNull("OntologyID was null", loadedOntologyID);
        
        Assert.assertNull("Was not an anonymous ontology", loadedOntologyID.getOntologyIRI());
        Assert.assertNull("Was not an anonymous ontology", loadedOntologyID.getVersionIRI());
        
        final OWLOntology loadedOntology = this.manager.getOntology(loadedOntologyID);
        Assert.assertNotNull("Ontology not in memory", loadedOntology);
        Assert.assertFalse("Ontology is empty", loadedOntology.isEmpty());
        Assert.assertEquals("Not the expected number of axioms", 1, loadedOntology.getAxiomCount());
    }
    
    /**
     * Test method for {@link com.github.podd.api.PoddOWLManager#getReasonerProfiles()} .
     * 
     */
    @Test
    public void testGetReasonerProfile() throws Exception
    {
        final Set<OWLProfile> profiles = ((PoddOWLManagerImpl)this.testOWLManager).getReasonerProfiles();
        Assert.assertNotNull("OWLProfile was null", profiles);
        Assert.assertFalse("OWLProfiles were not found for reasoner", profiles.isEmpty());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testGenerateInferredOntologyID() throws Exception
    {
        // prepare: create an OntologyID
        final OWLOntologyID ontologyID =
                new OWLOntologyID(IRI.create("http://purl.org/podd/ns/poddBase"),
                        IRI.create("http://purl.org/podd/ns/version/poddBase/1"));
        
        final InferredOWLOntologyID inferredOntologyID =
                ((PoddOWLManagerImpl)this.testOWLManager).generateInferredOntologyID(ontologyID);
        
        // verify:
        Assert.assertNotNull("InferredOntologyID was null", inferredOntologyID);
        Assert.assertNotNull("Inferred Ontology IRI was null", inferredOntologyID.getInferredOntologyIRI());
        Assert.assertEquals("Inferred IRI was not as expected",
                IRI.create(PODD.INFERRED_PREFIX + "http://purl.org/podd/ns/version/poddBase/1"),
                inferredOntologyID.getInferredOntologyIRI());
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testGenerateInferredOntologyIDWithEmptyOntologyID() throws Exception
    {
        // prepare: create an OntologyID
        final OWLOntologyID ontologyID = new OWLOntologyID();
        
        try
        {
            ((PoddOWLManagerImpl)this.testOWLManager).generateInferredOntologyID(ontologyID);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddOWLManager#generateInferredOntologyID(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     * 
     */
    @Test
    public void testGenerateInferredOntologyIDWithNullOntologyID() throws Exception
    {
        try
        {
            ((PoddOWLManagerImpl)this.testOWLManager).generateInferredOntologyID(null);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected Exception", "OWLOntology is incomplete", e.getMessage());
        }
    }
    
}
