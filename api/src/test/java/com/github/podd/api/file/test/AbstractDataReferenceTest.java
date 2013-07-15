/**
 * 
 */
package com.github.podd.api.file.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;

import com.github.podd.api.file.DataReference;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Simple abstract test class for DataReference
 * 
 * @author kutila
 */
public abstract class AbstractDataReferenceTest
{
    protected DataReference dataReference;
    
    /**
     * 
     * @return A new DataReference instance for use by the test
     */
    protected abstract DataReference getNewDataReference();
    
    @Before
    public void setUp() throws Exception
    {
        this.dataReference = this.getNewDataReference();
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.dataReference = null;
    }
    
    @Test
    public void testGetArtifactID() throws Exception
    {
        this.dataReference.getArtifactID();
    }
    
    @Test
    public void testGetLabel() throws Exception
    {
        this.dataReference.getLabel();
    }
    
    @Test
    public void testGetObjectIRI() throws Exception
    {
        this.dataReference.getObjectIri();
    }
    
    @Test
    public void testGetRepositoryAlias() throws Exception
    {
        this.dataReference.getRepositoryAlias();
    }
    
    @Test
    public void testSetArtifactID() throws Exception
    {
        final InferredOWLOntologyID ontologyID =
                new InferredOWLOntologyID(IRI.create("urn:test:ontologyiri:abc"),
                        IRI.create("urn:test:versioniri:abc:version:44"),
                        IRI.create("urn:test:inferred:versioniri:abc:version:44"));
        
        this.dataReference.setArtifactID(ontologyID);
    }
    
    @Test
    public void testSetLabel() throws Exception
    {
        this.dataReference.setLabel("Test Label");
    }
    
    @Test
    public void testSetObjectIRI() throws Exception
    {
        final IRI objectIri = IRI.create("urn:test:objectiri:podd-object:4a");
        this.dataReference.setObjectIri(objectIri);
    }
    
    @Test
    public void testSetRepositoryAlias() throws Exception
    {
        this.dataReference.setRepositoryAlias("Test Repository Alias");
    }
    
}
