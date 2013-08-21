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
