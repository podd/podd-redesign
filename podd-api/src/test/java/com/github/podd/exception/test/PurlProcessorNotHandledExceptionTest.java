/**
 * 
 */
package com.github.podd.exception.test;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;
import org.openrdf.model.URI;

import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.exception.PurlProcessorNotHandledException;

/**
 * Tests that the PurlProcessorNotHandledException correctly manages its internal content.
 * 
 * @author kutila
 */
public class PurlProcessorNotHandledExceptionTest
{
    
    @Test
    public void testPurlGeneratorNotHandledException() throws Exception
    {
        final PoddPurlProcessor thePurlProcessor = Mockito.mock(PoddPurlProcessor.class);
        final URI failedUri = Mockito.mock(URI.class);
        final String message = "Exception with mocked up internals";
        
        final PurlProcessorNotHandledException theException =
                new PurlProcessorNotHandledException(thePurlProcessor, failedUri, message);
        
        Assert.assertNotNull(theException.getGenerator());
        Assert.assertEquals(failedUri, theException.getInputUri());
        Assert.assertEquals(message, theException.getMessage());
    }
    
}
