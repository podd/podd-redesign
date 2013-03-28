/**
 * 
 */
package com.github.podd.api.file.test;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.SSHFileReference;

/**
 * Simple abstract test class for SSHFileReference
 * 
 * @author kutila
 */
public abstract class AbstractSSHFileReferenceTest extends AbstractFileReferenceTest
{
    protected SSHFileReference sshFileReference;
    
    /**
     * 
     * @return A new SSHFileReference instance for use by the test
     */
    protected abstract SSHFileReference getNewSSHFileReference();
    
    protected final FileReference getNewFileReference() 
    {
        return this.getNewSSHFileReference();
    }
    
    @Before
    public void setUp() throws Exception
    {
        super.setUp();
        this.sshFileReference = this.getNewSSHFileReference();
    }
    
    @After
    public void tearDown() throws Exception
    {
        super.tearDown();
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
