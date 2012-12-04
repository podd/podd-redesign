/**
 * 
 */
package com.github.podd.impl.purl.test;

import com.github.podd.api.PoddRdfProcessorFactory;
import com.github.podd.api.test.AbstractPoddRdfProcessorFactoryTest;
import com.github.podd.impl.purl.SimpleUUIDPurlProcessor;
import com.github.podd.impl.purl.SimpleUUIDPurlProcessorFactory;

/**
 * @author kutila
 *
 */
public class SimpleUUIDPurlProcessorFactoryTest extends AbstractPoddRdfProcessorFactoryTest<SimpleUUIDPurlProcessor>
{

    @Override
    protected PoddRdfProcessorFactory<SimpleUUIDPurlProcessor> getNewPoddRdfProcessorFactory()
    {
        SimpleUUIDPurlProcessorFactory factory = new SimpleUUIDPurlProcessorFactory();
        factory.setTemporaryUriArray(new String[] {"urn:temp", "urn:temporary"});
        return (PoddRdfProcessorFactory) factory;
    }
    
    //TODO: run generated SPARQL query against an in-memory store and verify expected results
    
}
