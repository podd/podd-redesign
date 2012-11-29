/**
 * 
 */
package com.github.podd.api.purl.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;

import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.PoddPurlReference;
import com.github.podd.exception.PurlGeneratorNotHandledException;

/**
 * Abstract class to test PoddPurlProcessor.
 * 
 * @author kutila
 * 
 */
public abstract class AbstractPoddPurlProcessorTest
{
    
    private PoddPurlProcessor purlProcessor;
    
    private String prefixUrnTemp = null;
    private String prefixExampleUrl = null;
    private String prefixPurl = null;
    
    /**
     * @return A new PoddPurlProcessor instance for use by the test
     */
    protected abstract PoddPurlProcessor getNewPoddPurlProcessor();
    
    /**
     * Checks whether the given purl could have been generated from the given temporary URI.
     * <p/>
     * Note: Since purl generation may involve some randomness, this method may only be able to make
     * a partial comparison to see if it could have originated as the given temporary URI.
     * 
     * @param purl
     * @param tempUri
     * @return True if the Purl could have been generated from the tempUri. False otherwise
     */
    protected abstract boolean isPurlGeneratedFromTemp(URI purl, URI tempUri);
    
    @Before
    public void setUp() throws Exception
    {
        this.purlProcessor = this.getNewPoddPurlProcessor();
        
        this.prefixUrnTemp = "urn:temp:";
        this.prefixExampleUrl = "http://example.org/";
        this.prefixPurl = "http://purl.org/";
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.purlProcessor = null;
        
        this.prefixUrnTemp = null;
        this.prefixExampleUrl = null;
        this.prefixPurl = null;
    }
    
    @Test
    public void testAddTemporaryUriHandler() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixPurl);
    }
    
    @Test
    public void testCanHandleWithNoPrefixes() throws Exception
    {
        // we're not adding any temporary URI handlers.
        
        final URI tempUriUnsupported = new ValueFactoryImpl().createURI("urn:unsupported:temporary/uri");
        Assert.assertFalse(this.purlProcessor.canHandle(tempUriUnsupported));
    }
    
    @Test
    public void testCanHandleWithOnePrefix() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        
        final URI tempUriUrnTemp = new ValueFactoryImpl().createURI(this.prefixUrnTemp + "some/path");
        Assert.assertTrue(this.getNewPoddPurlProcessor().canHandle(tempUriUrnTemp));
    }
    
    @Test
    public void testCanHandleWithTwoPrefixes() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        this.purlProcessor.addTemporaryUriHandler(this.prefixExampleUrl);
        
        final URI tempUriUrnTemp = new ValueFactoryImpl().createURI(this.prefixUrnTemp + "some/path");
        Assert.assertTrue(this.getNewPoddPurlProcessor().canHandle(tempUriUrnTemp));
        
        final URI tempUriExampleOrg = new ValueFactoryImpl().createURI(this.prefixExampleUrl + "some/other/path");
        Assert.assertTrue(this.getNewPoddPurlProcessor().canHandle(tempUriExampleOrg));
    }
    
    /**
     * Test a PoddPurlProcessor that has not been assigned any temporary URI prefixes
     * 
     * @throws Exception
     */
    @Test
    public void testGetTemporaryUriHandlersWithNoPrefixes() throws Exception
    {
        final List<String> prefixList = this.purlProcessor.getTemporaryUriHandlers();
        Assert.assertNotNull(prefixList);
        Assert.assertEquals(0, prefixList.size());
    }
    
    @Test
    public void testGetTemporaryUriHandlersWithTwoPrefixes() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        this.purlProcessor.addTemporaryUriHandler(this.prefixExampleUrl);
        
        final List<String> prefixList = this.purlProcessor.getTemporaryUriHandlers();
        Assert.assertNotNull(prefixList);
        Assert.assertEquals(2, prefixList.size());
        Assert.assertTrue(prefixList.contains(this.prefixUrnTemp));
        Assert.assertTrue(prefixList.contains(this.prefixExampleUrl));
        
        // this unregistered prefix is not in the list
        Assert.assertFalse(prefixList.contains(this.prefixPurl));
    }
    
    @Test
    public void testRemoveTemporaryUriHandler() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        this.purlProcessor.addTemporaryUriHandler(this.prefixExampleUrl);
        
        final URI tempUriUrnTemp = new ValueFactoryImpl().createURI(this.prefixUrnTemp + "some/path");
        Assert.assertTrue(this.getNewPoddPurlProcessor().canHandle(tempUriUrnTemp));
        
        this.purlProcessor.removeTemporaryUriHandler(this.prefixUrnTemp);
        
        // tempUriUrnTemp is no longer supported
        Assert.assertFalse(this.getNewPoddPurlProcessor().canHandle(tempUriUrnTemp));
        
        // prefixExampleUrl is still supported
        final URI tempUriExampleOrg = new ValueFactoryImpl().createURI(this.prefixExampleUrl + "some/other/path");
        Assert.assertTrue(this.getNewPoddPurlProcessor().canHandle(tempUriExampleOrg));
    }
    
    @Test
    public void testHandleTranslationUnSupported() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        final URI tempUriUrnTemp = new ValueFactoryImpl().createURI(this.prefixExampleUrl + ":artifact:8275");
        
        try
        {
            this.purlProcessor.handleTranslation(tempUriUrnTemp);
            Assert.fail("Expected PurlGeneratorNotHandledException was not thrown");
        }
        catch(final PurlGeneratorNotHandledException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testHandleTranslationSuccessful() throws Exception
    {
        this.purlProcessor.addTemporaryUriHandler(this.prefixUrnTemp);
        final URI tempUriUrnTemp = new ValueFactoryImpl().createURI(this.prefixUrnTemp + ":artifact:1482");
        
        final PoddPurlReference purlReference = this.purlProcessor.handleTranslation(tempUriUrnTemp);
        
        Assert.assertNotNull(purlReference);
        Assert.assertEquals(tempUriUrnTemp, purlReference.getTemporaryURI());
        Assert.assertTrue(this.isPurlGeneratedFromTemp(purlReference.getPurlURI(), tempUriUrnTemp));
    }
    
}
