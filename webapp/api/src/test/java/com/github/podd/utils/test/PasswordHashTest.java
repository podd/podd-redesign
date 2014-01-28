package com.github.podd.utils.test;

import org.junit.Assert;
import org.junit.Test;

import com.github.podd.utils.PasswordHash;

public class PasswordHashTest
{
    @Test
    public final void testCreateHashString() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            final String password = Integer.toHexString(i);
            final String hash = PasswordHash.createHash(password);
            final String secondHash = PasswordHash.createHash(password);
            
            Assert.assertNotNull(hash);
            Assert.assertNotNull(secondHash);
            
            Assert.assertNotEquals(hash, secondHash);
        }
    }
    
    @Test
    public final void testCreateHashCharArray() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            final String password = Integer.toHexString(i);
            final String hash = PasswordHash.createHash(password.toCharArray());
            final String secondHash = PasswordHash.createHash(password.toCharArray());
            
            Assert.assertNotNull(hash);
            Assert.assertNotNull(secondHash);
            
            Assert.assertNotEquals(hash, secondHash);
        }
    }
    
    @Test
    public final void testValidatePasswordStringString() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            final String password = Integer.toHexString(i);
            final String hash = PasswordHash.createHash(password);
            
            Assert.assertNotNull(hash);
            
            Assert.assertTrue(PasswordHash.validatePassword(password, hash));
            Assert.assertFalse(PasswordHash.validatePassword(Integer.toHexString(i + 1), hash));
        }
    }
    
    @Test
    public final void testValidatePasswordCharArrayString() throws Exception
    {
        for(int i = 0; i < 10; i++)
        {
            final String password = Integer.toHexString(i);
            final String hash = PasswordHash.createHash(password.toCharArray());
            
            Assert.assertNotNull(hash);
            
            Assert.assertTrue(PasswordHash.validatePassword(password.toCharArray(), hash));
            
            Assert.assertFalse(PasswordHash.validatePassword(Integer.toHexString(i + 1).toCharArray(), hash));
        }
    }
    
}
