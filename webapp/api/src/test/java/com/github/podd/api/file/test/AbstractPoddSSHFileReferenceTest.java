/**
 * 
 */
package com.github.podd.api.file.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.file.PoddSSHFileReference;

/**
 * Simple abstract test class for PoddSSHFileReference
 * 
 * @author kutila
 */
public abstract class AbstractPoddSSHFileReferenceTest
{
    protected PoddSSHFileReference sshFileReference;
    
    /**
     * 
     * @return A new PoddSSHFileReference instance for use by the test
     */
    protected abstract PoddSSHFileReference getNewPoddSSHFileReference();
    
    @Before
    public void setUp() throws Exception
    {
        this.sshFileReference = this.getNewPoddSSHFileReference();
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.sshFileReference = null;
    }
    
    @Test
    public void testGetFilename() throws Exception
    {
        this.sshFileReference.getFilename();
    }
    
    @Test
    public void testGetPath() throws Exception
    {
        this.sshFileReference.getPath();
    }

    @Test
    public void testSetFilename() throws Exception
    {
        this.sshFileReference.setFilename("plant-134.54-imageset-12343452.zip");
    }
    
    @Test
    public void testSetPath() throws Exception
    {
        this.sshFileReference.setPath("/path/to/file");
    }
    
}
