/**
 * 
 */
package com.github.podd.api.file.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.api.file.FileReference;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Simple abstract test class for FileReference
 * 
 * @author kutila
 */
public abstract class AbstractFileReferenceTest
{
    protected FileReference fileReference;
    
    /**
     * 
     * @return A new FileReference instance for use by the test
     */
    protected abstract FileReference getNewFileReference();
    
    @Before
    public void setUp() throws Exception
    {
        this.fileReference = this.getNewFileReference();
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
    public void testGetLabel() throws Exception
    {
        this.fileReference.getLabel();
    }
    
    @Test
    public void testGetObjectIRI() throws Exception
    {
        this.fileReference.getObjectIri();
    }
    
    @Test
    public void testGetRepositoryAlias() throws Exception
    {
        this.fileReference.getRepositoryAlias();
    }
    
    @Test
    public void testSetArtifactID() throws Exception
    {
        final InferredOWLOntologyID ontologyID =
                new InferredOWLOntologyID(IRI.create("urn:test:ontologyiri:abc"),
                        IRI.create("urn:test:versioniri:abc:version:44"),
                        IRI.create("urn:test:inferred:versioniri:abc:version:44"));
        
        this.fileReference.setArtifactID(ontologyID);
    }
    
    @Test
    public void testSetLabel() throws Exception
    {
        this.fileReference.setLabel("Test Label");
    }
    
    @Test
    public void testSetObjectIRI() throws Exception
    {
        final IRI objectIri = IRI.create("urn:test:objectiri:podd-object:4a");
        this.fileReference.setObjectIri(objectIri);
    }
    
    @Test
    public void testSetRepositoryAlias() throws Exception
    {
        this.fileReference.setRepositoryAlias("Test Repository Alias");
    }
    
}
