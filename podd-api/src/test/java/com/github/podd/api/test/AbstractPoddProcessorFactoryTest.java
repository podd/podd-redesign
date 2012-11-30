/**
 * 
 */
package com.github.podd.api.test;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.PoddProcessor;
import com.github.podd.api.PoddProcessorFactory;

/**
 * Abstract class to test PoddProcessorFactory
 * 
 * @author kutila
 * 
 */
public abstract class AbstractPoddProcessorFactoryTest<T extends PoddProcessor<I>, I>
{
    protected PoddProcessorFactory<T, I> processorFactory;
    
    /**
     * @return A new PoddProcessorFactory instance for use in the test
     */
    protected abstract PoddProcessorFactory<T, I> getNewPoddProcessorFactory();
    
    @Before
    public void setUp() throws Exception
    {
        this.processorFactory = this.getNewPoddProcessorFactory();
        Assert.assertNotNull("Null implementation of test processor factory", this.processorFactory);
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.processorFactory = null;
    }
    
    @Test
    public void testCanHandleStage() throws Exception
    {
        
    }
    
    @Test
    public void testGetProcessor() throws Exception
    {
        final PoddProcessor poddProcessor = this.processorFactory.getProcessor();
        Assert.assertNotNull("Received a null Podd Processor", poddProcessor);
    }
    
    @Test
    public void testGetKey() throws Exception
    {
        
    }
    
    @Test
    public void testGetStages() throws Exception
    {
        
    }
    
}
