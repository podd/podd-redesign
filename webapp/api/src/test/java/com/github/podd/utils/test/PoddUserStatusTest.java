/*
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
 /**
 * 
 */
package com.github.podd.utils.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.podd.utils.PoddUserStatus;

/**
 * Tests the supported PODD User Statuses
 * 
 * @author kutila
 */
public class PoddUserStatusTest
{
    
    @Test
    public void testPoddUserStatus() throws Exception
    {
        final PoddUserStatus[] allStatuses = PoddUserStatus.values();
        Assert.assertEquals(2, allStatuses.length);
        
        Assert.assertNotNull(PoddUserStatus.valueOf("ACTIVE"));
        Assert.assertNotNull(PoddUserStatus.valueOf("INACTIVE"));
        
        try
        {
            PoddUserStatus.valueOf("BUSY"); // invalid status
        }
        catch(final IllegalArgumentException e)
        {
            Assert.assertNotNull(e);
        }
        
    }
}
