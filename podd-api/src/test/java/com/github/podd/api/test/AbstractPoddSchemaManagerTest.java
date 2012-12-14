/**
 * 
 */
package com.github.podd.api.test;

import java.io.InputStream;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.exception.EmptyOntologyException;

/**
 * Abstract test to verify that the PoddSchemaManager API contract is followed by implementations.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class AbstractPoddSchemaManagerTest
{
    private PoddSchemaManager testSchemaManager;
    private PoddOWLManager testOwlManager;
    private PoddRepositoryManager testRepositoryManager;
    private OWLOntologyManager owlapiManager;
    
    /**
     * 
     * @return A new instance of PoddOWLManager, for each call to this method.
     */
    protected abstract PoddOWLManager getNewPoddOwlManagerInstance();
    
    /**
     * 
     * @return A new instance of OWLOntologyManager, for each call to this method.
     */
    protected abstract OWLOntologyManager getNewOwlOntologyManagerInstance();
    
    /**
     * 
     * @return A new instance of PoddRepositoryManager, for each call to this method.
     */
    protected abstract PoddRepositoryManager getNewPoddRepositoryManagerInstance();
    
    /**
     * 
     * @return A new instance of PoddSchemaManager, for each call to this method.
     */
    protected abstract PoddSchemaManager getNewPoddSchemaManagerInstance();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testSchemaManager = this.getNewPoddSchemaManagerInstance();
        
        this.testRepositoryManager = this.getNewPoddRepositoryManagerInstance();
        this.testSchemaManager.setRepositoryManager(this.testRepositoryManager);
        
        this.testOwlManager = this.getNewPoddOwlManagerInstance();
        this.owlapiManager = this.getNewOwlOntologyManagerInstance();
        this.testOwlManager.setOWLOntologyManager(owlapiManager);
        this.testSchemaManager.setOwlManager(this.testOwlManager);
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testSchemaManager = null;
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyMatchingOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyNotMatchingVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyNullOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyNullOutputStream() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyOnlyOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesMatchingOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesNoInferencesFound() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesNotMatchingVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesNullOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesNullOutputStream() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntologyWithInferences(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testDownloadSchemaOntologyWithInferencesOnlyOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetCurrentSchemaOntologyVersionMatchesOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetCurrentSchemaOntologyVersionMatchesOntologyVersionIRICurrent() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetCurrentSchemaOntologyVersionMatchesOntologyVersionIRINotCurrent() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetCurrentSchemaOntologyVersionNoMatches() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetCurrentSchemaOntologyVersionNull() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
     * .
     * 
     * Test that the designated current version is retrieved when an ontology IRI is given and there
     * are multiple versions available.
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyIRIMatchesOntologyIRIMultipleVersions() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyIRIMatchesOntologyIRIOneVersion() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyIRIMatchesVersionIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.IRI)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyIRINull() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDNullOntologyID() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDNullVersionIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDOntologyExists() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDOntologyIRIDoesNotExist() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#getSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testGetSchemaOntologyOWLOntologyIDOntologyVersionDoesNotExist() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testSetCurrentSchemaOntologyVersionOneVersionOfOntology() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testSetCurrentSchemaOntologyVersionTwoVersionsOfOntologyChange() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#setCurrentSchemaOntologyVersion(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Ignore
    @Test
    public final void testSetCurrentSchemaOntologyVersionTwoVersionsOfOntologyNoChange() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyEmpty() throws Exception
    {
        try
        {
            InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/empty.owl");
            
            this.testSchemaManager.uploadSchemaOntology(testInputStream, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(EmptyOntologyException e)
        {
            Assert.assertEquals("Message was not as expected", "Loaded ontology is empty", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyIDOverrideEmpty() throws Exception
    {
        try
        {
            IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
            IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
            OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
            this.owlapiManager.createOntology(emptyOntologyID);
            
            InputStream testInputStream = this.getClass().getResourceAsStream("/test/ontologies/empty.owl");
            
            this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, testInputStream, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(EmptyOntologyException e)
        {
            Assert.assertEquals("Message was not as expected", "Loaded ontology is empty", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideInvalidRdfXml() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideInvalidTurtle() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideNoOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideNotConsistent() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideNotInProfile() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyIDOverrideNullInput() throws Exception
    {
        try
        {
            IRI emptyOntologyIRI = IRI.create("urn:test:empty:ontology:");
            IRI emptyVersionIRI = IRI.create("urn:test:empty:version:");
            OWLOntologyID emptyOntologyID = new OWLOntologyID(emptyOntologyIRI, emptyVersionIRI);
            this.owlapiManager.createOntology(emptyOntologyID);
            
            this.testSchemaManager.uploadSchemaOntology(emptyOntologyID, null, RDFFormat.RDFXML);
            
            Assert.fail("Did not receive expected exception");
        }
        catch(NullPointerException e)
        {
            Assert.assertEquals("Message was not as expected", "Schema Ontology input stream was null", e.getMessage());
        }
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideOnlyOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyIDOverrideWithOntologyIRIAndVersionIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyInvalidRdfXml() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyInvalidTurtle() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyNoOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyNotConsistent() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyNotInProfile() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyNullInput() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyOnlyOntologyIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Ignore
    @Test
    public final void testUploadSchemaOntologyWithOntologyIRIAndVersionIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
}
