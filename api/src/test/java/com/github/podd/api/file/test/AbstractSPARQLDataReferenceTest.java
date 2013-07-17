/**
 * 
 */
package com.github.podd.api.file.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.file.DataReference;
import com.github.podd.api.file.SPARQLDataReference;

/**
 * Simple abstract test class for SSHFileReference
 * 
 * @author kutila
 */
public abstract class AbstractSPARQLDataReferenceTest extends AbstractDataReferenceTest
{
    protected SPARQLDataReference sparqlDataReference;
    
    @Override
    protected final DataReference getNewDataReference()
    {
        return this.getNewSPARQLDataReference();
    }
    
    /**
     * 
     * @return A new SPARQLDataReference instance for use by the test
     */
    protected abstract SPARQLDataReference getNewSPARQLDataReference();
    
    @Override
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.sparqlDataReference = this.getNewSPARQLDataReference();
    }
    
    @Override
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
        this.sparqlDataReference = null;
    }
    
    @Test
    public void testGetGraph() throws Exception
    {
        this.sparqlDataReference.getGraph();
    }
    
    @Test
    public void testSetGraph() throws Exception
    {
        this.sparqlDataReference.setGraph("urn:test:graph:1");
    }
    
}
