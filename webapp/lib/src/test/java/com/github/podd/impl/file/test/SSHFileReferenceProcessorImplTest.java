/**
 * 
 */
package com.github.podd.impl.file.test;

import com.github.podd.api.file.SSHFileReferenceProcessor;
import com.github.podd.api.file.test.AbstractSSHFileReferenceProcessorTest;
import com.github.podd.impl.file.SSHFileReferenceProcessorImpl;

/**
 * 
 * @author kutila
 */
public class SSHFileReferenceProcessorImplTest extends AbstractSSHFileReferenceProcessorTest
{
    
    @Override
    protected SSHFileReferenceProcessor getNewSSHFileReferenceProcessor()
    {
        return new SSHFileReferenceProcessorImpl();
    }
    
}
