/**
 * 
 */
package com.github.podd.impl.purl.test;

import org.openrdf.model.URI;

import com.github.podd.api.purl.PoddPurlProcessor;
import com.github.podd.api.purl.test.AbstractPoddPurlProcessorTest;
import com.github.podd.impl.purl.SimplePoddPurlProcessor;

/**
 * @author kutila
 * 
 */
public class SimplePoddPurlProcessorNonDefaultPrefixTest extends AbstractPoddPurlProcessorTest
{
    
    private final String purlPrefix = "urn:example:purl";
    
    @Override
    protected PoddPurlProcessor getNewPoddPurlProcessor()
    {
        return new SimplePoddPurlProcessor(this.purlPrefix);
    }
    
    @Override
    protected boolean isPurlGeneratedFromTemp(final URI purl, final URI tempUri)
    {
        // NOTE: This method will start giving incorrect results when
        // multiple PurlProcessors use the same prefix
        return purl.stringValue().startsWith(this.purlPrefix);
    }
    
}
