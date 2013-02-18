/**
 * 
 */
package com.github.podd.exception.test;

import org.junit.Assert;

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
    public void testConstructorWithProcessorUriMessage() throws Exception
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
    
    @Test
    public void testConstructorWithProcessorUriMessageThrowable() throws Exception
    {
        final PoddPurlProcessor thePurlProcessor = Mockito.mock(PoddPurlProcessor.class);
        final URI failedUri = Mockito.mock(URI.class);
        final String message = "Exception with mocked up internals";
        final Exception rootCause = new Exception("root cause exception");
        
        final PurlProcessorNotHandledException theException =
                new PurlProcessorNotHandledException(thePurlProcessor, failedUri, message, rootCause);
        
        Assert.assertNotNull(theException.getGenerator());
        Assert.assertEquals(failedUri, theException.getInputUri());
        Assert.assertEquals(message, theException.getMessage());
        Assert.assertEquals(rootCause, theException.getCause());
    }
    
    @Test
    public void testConstructorWithProcessorUriThrowable() throws Exception
    {
        final PoddPurlProcessor thePurlProcessor = Mockito.mock(PoddPurlProcessor.class);
        final URI failedUri = Mockito.mock(URI.class);
        final Exception rootCause = new Exception("root cause of this exception");
        
        final PurlProcessorNotHandledException theException =
                new PurlProcessorNotHandledException(thePurlProcessor, failedUri, rootCause);
        
        Assert.assertNotNull(theException.getGenerator());
        Assert.assertEquals(failedUri, theException.getInputUri());
        Assert.assertEquals(rootCause, theException.getCause());
    }
    
}
