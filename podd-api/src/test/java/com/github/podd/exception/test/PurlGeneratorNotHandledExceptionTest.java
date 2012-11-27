/**
 * 
 */
package com.github.podd.exception.test;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.URI;

import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.exception.PurlGeneratorNotHandledException;

/**
 * Tests that the PurlGeneratorNotHandledException correctly manages its internal content.
 * 
 * @author kutila
 */
public class PurlGeneratorNotHandledExceptionTest
{
    
    @Test
    public void testPurlGeneratorNotHandledException() throws Exception
    {
        final PoddPurlProcessor thePurlProcessor = Mockito.mock(PoddPurlProcessor.class);
        final URI failedUri = Mockito.mock(URI.class);
        final String message = "Exception with mocked up internals";
        
        final PurlGeneratorNotHandledException theException =
                new PurlGeneratorNotHandledException(thePurlProcessor, failedUri, message);
        
        Assert.assertNotNull(theException.getGenerator());
        Assert.assertEquals(failedUri, theException.getInputUri());
        Assert.assertEquals(message, theException.getMessage());
    }
    
}
