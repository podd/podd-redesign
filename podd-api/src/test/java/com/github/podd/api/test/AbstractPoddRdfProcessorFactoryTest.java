/**
 * 
 */
package com.github.podd.api.test;

import com.github.podd.api.PoddProcessorFactory;
import com.github.podd.api.PoddRdfProcessorFactory;

/**
 * @author kutila
 *
 */
public abstract class AbstractPoddRdfProcessorFactoryTest  extends AbstractPoddProcessorFactoryTest
{

    @Override
    protected final PoddProcessorFactory getNewPoddProcessorFactory()
    {
        return getNewPoddRdfProcessorFactory();
    }

    protected abstract PoddRdfProcessorFactory getNewPoddRdfProcessorFactory();
    
}
