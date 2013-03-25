/**
 * 
 */
package com.github.podd.impl.file.test;

import com.github.podd.api.file.PoddSSHFileReference;
import com.github.podd.api.file.test.AbstractPoddSSHFileReferenceTest;
import com.github.podd.impl.file.SimplePoddSSHFileReference;

/**
 * @author kutila
 *
 */
public class SimplPoddSSHFileReferenceTest extends AbstractPoddSSHFileReferenceTest
{

    @Override
    protected PoddSSHFileReference getNewPoddSSHFileReference()
    {
        PoddSSHFileReference ref = new SimplePoddSSHFileReference();
        return ref;
    }
    
}
