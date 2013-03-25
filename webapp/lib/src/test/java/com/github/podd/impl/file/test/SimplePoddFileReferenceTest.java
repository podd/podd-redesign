/**
 * 
 */
package com.github.podd.impl.file.test;

import com.github.podd.api.file.PoddFileReference;
import com.github.podd.api.file.test.AbstractPoddFileReferenceTest;
import com.github.podd.impl.file.SimplePoddSSHFileReference;

/**
 * @author kutila
 *
 */
public class SimplePoddFileReferenceTest extends AbstractPoddFileReferenceTest
{

    @Override
    protected PoddFileReference getNewPoddFileReference()
    {
        return new SimplePoddSSHFileReference();
    }
    
}
