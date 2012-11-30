/**
 * 
 */
package com.github.podd.api.purl.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.purl.PoddPurlReference;

/**
 * Simple abstract test class for PoddPurlReference
 * 
 * @author kutila
 *
 */
public abstract class AbstractPoddPurlReferenceTest
{
  
    protected PoddPurlReference purlReference;
    
    /**
     * 
     * @return A new PoddPurlReference instance for use by the test
     */
    protected abstract PoddPurlReference getNewPoddPurlReference();

    @Before
    public void setUp() throws Exception
    {
        this.purlReference = this.getNewPoddPurlReference();
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.purlReference = null;
    }
    
    @Test
    public void testGetPurlURI() throws Exception
    {
        this.purlReference.getPurlURI();
    }
    
    @Test
    public void testGetTemporaryURI() throws Exception
    {
        this.purlReference.getTemporaryURI();
    }
}
