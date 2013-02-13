/**
 * 
 */
package com.github.podd.api.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.podd.api.PoddProcessorStage;

/**
 * Tests the supported PODD Processor Stages
 * 
 * @author kutila
 * 
 */
public class PoddProcessorStageTest
{
    
    @Test
    public void testPoddProcessorStage() throws Exception
    {
        final PoddProcessorStage[] allStatuses = PoddProcessorStage.values();
        Assert.assertEquals("Not the expected number of PODD Processor Stages", 7, allStatuses.length);
        
        // check each of the available statuses
        Assert.assertNotNull(PoddProcessorStage.valueOf("RDF_PARSING"));
        Assert.assertNotNull(PoddProcessorStage.valueOf("OWL_AXIOM"));
        Assert.assertNotNull(PoddProcessorStage.valueOf("PROFILE_CHECK"));
        Assert.assertNotNull(PoddProcessorStage.valueOf("CONSISTENCY_CHECK"));
        Assert.assertNotNull(PoddProcessorStage.valueOf("CONCRETE_AXIOM_STORAGE"));
        Assert.assertNotNull(PoddProcessorStage.valueOf("INFERENCE"));
        Assert.assertNotNull(PoddProcessorStage.valueOf("INFERRED_AXIOM_STORAGE"));
        
        // check a non-existent status
        try
        {
            PoddProcessorStage.valueOf("FILE_REFERENCING");
        }
        catch(final IllegalArgumentException e)
        {
            Assert.assertNotNull(e);
        }
    }
}
