/**
 * 
 */
package com.github.podd.impl.purl.test;

import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.api.purl.test.AbstractPoddPurlManagerTest;
import com.github.podd.impl.purl.PoddPurlManagerImpl;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;

/**
 * @author kutila
 * 
 */
public class PoddPurlManagerImplTest extends AbstractPoddPurlManagerTest
{
    
    @Override
    public PoddPurlManager getNewPoddPurlManager()
    {
        return new PoddPurlManagerImpl();
    }
    
    @Override
    public PoddPurlProcessorFactoryRegistry getNewPoddPurlProcessorFactoryRegistry()
    {
        final PoddPurlProcessorFactoryRegistry registry = PoddPurlProcessorFactoryRegistry.getInstance();
        registry.clear();
        
        final UUIDPurlProcessorFactoryImpl uuidPurlProcessorFactory = new UUIDPurlProcessorFactoryImpl();
        uuidPurlProcessorFactory.setPrefix("http://purl.org/podd/");
        registry.add(uuidPurlProcessorFactory);
        
        return registry;
    }
    
}
