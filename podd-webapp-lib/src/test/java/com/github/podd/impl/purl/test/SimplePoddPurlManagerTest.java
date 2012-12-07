/**
 * 
 */
package com.github.podd.impl.purl.test;

import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.api.purl.test.AbstractPoddPurlManagerTest;
import com.github.podd.impl.purl.SimplePoddPurlManager;
import com.github.podd.impl.purl.SimpleUUIDPurlProcessorFactory;

/**
 * @author kutila
 *
 */
public class SimplePoddPurlManagerTest extends AbstractPoddPurlManagerTest
{

    @Override
    public PoddPurlManager getNewPoddPurlManager()
    {
        return new SimplePoddPurlManager();
    }

    @Override
    public PoddPurlProcessorFactoryRegistry getNewPoddPurlProcessorFactoryRegistry()
    {
        PoddPurlProcessorFactoryRegistry registry = PoddPurlProcessorFactoryRegistry.getInstance();
        registry.clear();
        
        SimpleUUIDPurlProcessorFactory uuidPurlProcessorFactory = new SimpleUUIDPurlProcessorFactory();
        uuidPurlProcessorFactory.setPrefix("http://purl.org/podd/");
        registry.add(uuidPurlProcessorFactory);
        
        return registry;
    }
    
}
