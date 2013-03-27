/**
 * 
 */
package com.github.podd.api.file.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.api.file.PoddFileReference;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Simple abstract test class for PoddFileReference
 * 
 * @author kutila
 */
public abstract class AbstractPoddFileReferenceTest
{
    protected PoddFileReference fileReference;
    
    /**
     * 
     * @return A new PoddFileReference instance for use by the test
     */
    protected abstract PoddFileReference getNewPoddFileReference();
    
    @Before
    public void setUp() throws Exception
    {
        this.fileReference = this.getNewPoddFileReference();
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.fileReference = null;
    }
    
    @Test
    public void testGetArtifactID() throws Exception
    {
        this.fileReference.getArtifactID();
    }
    
    @Test
    public void testGetObjectIRI() throws Exception
    {
        this.fileReference.getObjectIri();
    }

    @Test
    public void testGetLabel() throws Exception
    {
        this.fileReference.getLabel();
    }

    @Test
    public void testGetRepositoryAlias() throws Exception
    {
        this.fileReference.getRepositoryAlias();
    }
    
    @Test
    public void testSetArtifactID() throws Exception
    {
        InferredOWLOntologyID ontologyID = new InferredOWLOntologyID(
                IRI.create("urn:test:ontologyiri:abc"), 
                IRI.create("urn:test:versioniri:abc:version:44"),
                IRI.create("urn:test:inferred:versioniri:abc:version:44")
                );
        
        this.fileReference.setArtifactID(ontologyID);
    }
    
    @Test
    public void testSetObjectIRI() throws Exception
    {
        IRI objectIri = IRI.create("urn:test:objectiri:podd-object:4a");
        this.fileReference.setObjectIri(objectIri);
    }

    @Test
    public void testSetLabel() throws Exception
    {
        this.fileReference.setLabel("Test Label");
    }

    @Test
    public void testSetRepositoryAlias() throws Exception
    {
        this.fileReference.setRepositoryAlias("Test Repository Alias");
    }

}
