/**
 * PODD is an OWL ontology database used for scientific project management
 *
 * Copyright (C) 2009-2013 The University Of Queensland
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
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
    private PoddProcessorFactory<T, I> processorFactory;
    
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
    
    /**
     * Tests that ProcessorFactory.getStages() returns an accurate representation of the stages
     * supported by a PoddProcessorFactory.
     *
     * @throws Exception
     */
    @Test
    public void testCanHandleStageWithAllStages() throws Exception
    {
        for(final PoddProcessorStage supportedStage : this.processorFactory.getStages())
        {
            Assert.assertTrue(this.processorFactory.canHandleStage(supportedStage));
        }
        
        for(final PoddProcessorStage aStage : PoddProcessorStage.values())
        {
            if(!this.processorFactory.getStages().contains(aStage))
            {
                Assert.assertFalse(this.processorFactory.canHandleStage(aStage));
            }
        }
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
    public void testGetKey() throws Exception
    {
        Assert.assertNotNull(this.processorFactory.getKey());
    }
    
    /**
     * Test that no null processors are returned by processor factory.
     *
     * @throws Exception
     */
    @Test
    public void testGetProcessorNotNull() throws Exception
    {
        final PoddProcessor<I> poddProcessor = this.processorFactory.getProcessor();
        Assert.assertNotNull("Podd Processor was null", poddProcessor);
    }
    
    @Test
    public void testGetStagesNotEmpty() throws Exception
    {
        final Set<PoddProcessorStage> stages = this.processorFactory.getStages();
        Assert.assertNotNull("getStages() returned a NULL Set", stages);
        Assert.assertFalse("Should not have returned an empty Set", stages.isEmpty());
    }
    
}
