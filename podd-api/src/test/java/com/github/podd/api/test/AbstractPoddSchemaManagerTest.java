/**
 * 
 */
package com.github.podd.api.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.PoddSchemaManager;

/**
 * Abstract test to verify that the PoddSchemaManager API contract is followed by implementations.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class AbstractPoddSchemaManagerTest
{
    /**
     * 
     * @return A new instance of PoddSchemaManager, which operates on an empty repository, for each
     *         call to this method.
     */
    protected abstract PoddSchemaManager getNewPoddSchemaManagerInstance();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
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
     * {@link com.github.podd.api.PoddSchemaManager#downloadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.OutputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
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
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testUploadSchemaOntologyIDOverrideEmpty() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
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
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddSchemaManager#uploadSchemaOntology(org.semanticweb.owlapi.model.OWLOntologyID, java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
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
    @Test
    public final void testUploadSchemaOntologyWithOntologyIRIAndVersionIRI() throws Exception
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
}
