/**
 * 
 */
package com.github.podd.impl.test;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.test.AbstractPoddRepositoryManagerTest;
import com.github.podd.impl.PoddRepositoryManagerImpl;

/**
 * @author kutila
 * 
 */
public class PoddRepositoryManagerImplTest extends AbstractPoddRepositoryManagerTest
{
    
    @Override
    protected PoddRepositoryManager getNewPoddRepositoryManagerInstance()
    {
        return new PoddRepositoryManagerImpl();
    }
    
}
