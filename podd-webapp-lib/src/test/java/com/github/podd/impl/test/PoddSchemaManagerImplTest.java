/**
 * 
 */
package com.github.podd.impl.test;

import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.test.AbstractPoddSchemaManagerTest;
import com.github.podd.impl.PoddSchemaManagerImpl;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 *
 */
public class PoddSchemaManagerImplTest extends AbstractPoddSchemaManagerTest
{
    @Override
    protected PoddSchemaManager getNewPoddSchemaManagerInstance()
    {
        return new PoddSchemaManagerImpl();
    }
    
}
