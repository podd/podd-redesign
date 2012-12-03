/**
 * 
 */
package com.github.podd.api.test;

import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.PoddProcessor;
import com.github.podd.api.PoddProcessorFactory;
import com.github.podd.api.PoddProcessorStage;

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
    
    /**
     * Set the Stages that can be handled by this particular Processor Factory
     * 
     * @param factory
     *            The PoddProcessorFactory whose handled stages are being updated
     * @param stages
     *            The PoddProcessorStages that are to be added to the "handled list of stages"
     */
    protected abstract void addProcessorFactoryStages(PoddProcessorFactory<T, I> factory, PoddProcessorStage... stages);
    
    /**
     * Adds the specified number of Processors to the ProcessorFactory
     * 
     * @param factory
     *            PODD Processors are to be added to this factory
     * @param noOfProcessors
     */
    protected abstract void addProcessorsToFactory(PoddProcessorFactory<T, I> factory, int noOfProcessors);
    
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
    public void testCanHandleStageWithNull() throws Exception
    {
        try
        {
            this.processorFactory.canHandleStage(null);
            Assert.fail("Expected NullPointerException was not thrown");
        }
        catch(final NullPointerException e)
        {
            Assert.assertTrue(e.getMessage().toLowerCase().contains("null"));
        }
    }
    
    @Test
    public void testCanHandleStageWithOneStage() throws Exception
    {
        final PoddProcessorStage[] supportedStages = { PoddProcessorStage.PROFILE_CHECK };
        final PoddProcessorStage[] unsupportedStages =
                { PoddProcessorStage.CONCRETE_AXIOM_STORAGE, PoddProcessorStage.RDF_PARSING,
                        PoddProcessorStage.CONSISTENCY_CHECK, PoddProcessorStage.INFERENCE,
                        PoddProcessorStage.INFERRED_AXIOM_STORAGE, PoddProcessorStage.OWL_AXIOM };
        this.addProcessorFactoryStages(this.processorFactory, supportedStages);
        
        this.assertCanHandleStage(supportedStages, unsupportedStages);
    }
    
    @Test
    public void testCanHandleStageWithTwoStages() throws Exception
    {
        final PoddProcessorStage[] supportedStages =
                { PoddProcessorStage.PROFILE_CHECK, PoddProcessorStage.RDF_PARSING };
        final PoddProcessorStage[] unsupportedStages =
                { PoddProcessorStage.CONCRETE_AXIOM_STORAGE, PoddProcessorStage.CONSISTENCY_CHECK,
                        PoddProcessorStage.INFERENCE, PoddProcessorStage.INFERRED_AXIOM_STORAGE,
                        PoddProcessorStage.OWL_AXIOM };
        this.addProcessorFactoryStages(this.processorFactory, supportedStages);
        
        this.assertCanHandleStage(supportedStages, unsupportedStages);
    }
    
    @Test
    public void testCanHandleStageWithAllStages() throws Exception
    {
        final PoddProcessorStage[] supportedStages = PoddProcessorStage.values();
        final PoddProcessorStage[] otherStages = {};
        this.addProcessorFactoryStages(this.processorFactory, supportedStages);
        
        this.assertCanHandleStage(supportedStages, otherStages);
    }
    
    protected void assertCanHandleStage(final PoddProcessorStage[] supported, final PoddProcessorStage[] unsupported)
    {
        for(final PoddProcessorStage thisStage : supported)
        {
            Assert.assertTrue(this.processorFactory.canHandleStage(thisStage));
        }
        for(final PoddProcessorStage otherStage : unsupported)
        {
            Assert.assertFalse(this.processorFactory.canHandleStage(otherStage));
        }
    }
    
    @Test
    public void testGetStagesWithNoSupportedStages() throws Exception
    {
        final Set<PoddProcessorStage> stages = this.processorFactory.getStages();
        Assert.assertNotNull("getStages() returned a NULL Set", stages);
        Assert.assertEquals("Should have returned an empty Set", 0, stages.size());
    }
    
    /**
     * Tests that ProcessorFactory.getStages() returns an accurate representation of the stages
     * supported by a PoddProcessorFactory.
     * 
     * @throws Exception
     */
    @Test
    public void testGetStagesWithOneStage() throws Exception
    {
        final PoddProcessorStage[] supportedStages = { PoddProcessorStage.PROFILE_CHECK };
        
        this.addProcessorFactoryStages(this.processorFactory, supportedStages);
        
        final Set<PoddProcessorStage> stages = this.processorFactory.getStages();
        
        Assert.assertEquals(1, stages.size());
        Assert.assertTrue(stages.contains(supportedStages[0]));
    }
    
    @Test
    public void testGetStagesWithThreeStages() throws Exception
    {
        final PoddProcessorStage[] supportedStages =
                { PoddProcessorStage.PROFILE_CHECK, PoddProcessorStage.OWL_AXIOM,
                        PoddProcessorStage.INFERRED_AXIOM_STORAGE };
        
        this.addProcessorFactoryStages(this.processorFactory, supportedStages);
        
        final Set<PoddProcessorStage> stages = this.processorFactory.getStages();
        
        Assert.assertEquals(3, stages.size());
        Assert.assertTrue(stages.contains(supportedStages[0]));
        Assert.assertTrue(stages.contains(supportedStages[1]));
        Assert.assertTrue(stages.contains(supportedStages[2]));
    }
    
    /**
     * Simple test that getProcessor() can be invoked
     * 
     * @throws Exception
     */
    @Test
    public void testGetProcessorSimple() throws Exception
    {
        this.processorFactory.getProcessor();
    }
    
    /**
     * Test that when no processors are recorded with the factory, NULL is returned
     * 
     * @throws Exception
     */
    @Test
    public void testGetProcessorFromEmptyFactory() throws Exception
    {
        final PoddProcessor poddProcessor = this.processorFactory.getProcessor();
        Assert.assertNull("Was expecting null for the Podd Processor", poddProcessor);
    }
    
    /**
     * Test that when no processors are recorded with the factory, NULL is returned
     * 
     * @throws Exception
     */
    @Test
    public void testGetProcessorSingleFactory() throws Exception
    {
        this.addProcessorsToFactory(this.processorFactory, 1);
        
        final PoddProcessor poddProcessor = this.processorFactory.getProcessor();
        
        Assert.assertNull("Was expecting null for the Podd Processor", poddProcessor);
    }
    
    @Test
    public void testGetKey() throws Exception
    {
        this.processorFactory.getKey();
    }
    
    @Test
    public void testGetKeyDifferent() throws Exception
    {
        final String key1 = this.processorFactory.getKey();
        Assert.assertNotNull("ProcessorFactory.getKey() returned a NULL", key1);
        
        final String key2 = this.getNewPoddProcessorFactory().getKey();
        Assert.assertNotNull("ProcessorFactory.getKey() returned a NULL", key1);
        
        Assert.assertFalse(key1.equals(key2));
        Assert.assertFalse(key2.equals(key1));
    }
    
}
