/**
 * 
 */
package com.github.podd.impl.test;

import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.test.AbstractPoddSesameManagerTest;
import com.github.podd.impl.PoddSesameManagerImpl;

/**
 * @author kutila
 * 
 */
public class PoddSesameManagerImplTest extends AbstractPoddSesameManagerTest
{
    
    @Override
    public PoddSesameManager getNewPoddSesameManagerInstance()
    {
        return new PoddSesameManagerImpl();
    }
    
}
