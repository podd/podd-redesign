/**
 * 
 */
package com.github.podd.impl.file.test;

import com.github.podd.api.file.SSHFileReference;
import com.github.podd.api.file.test.AbstractSSHFileReferenceTest;
import com.github.podd.impl.file.SSHFileReferenceImpl;

/**
 * @author kutila
 *
 */
public class SSHFileReferenceImplTest extends AbstractSSHFileReferenceTest
{

    @Override
    protected SSHFileReference getNewSSHFileReference()
    {
        SSHFileReference ref = new SSHFileReferenceImpl();
        return ref;
    }
    
}
