/**
 * 
 */
package com.github.podd.utils.test;

import junit.framework.Assert;

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
