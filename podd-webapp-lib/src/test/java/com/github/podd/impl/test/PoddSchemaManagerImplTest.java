/**
 * 
 */
package com.github.podd.impl.test;

import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyManagerFactoryRegistry;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.test.AbstractPoddSchemaManagerTest;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
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
    
    @Override
    protected PoddOWLManager getNewPoddOwlManagerInstance()
    {
        return new PoddOWLManagerImpl();
    }
    
    @Override
    protected OWLOntologyManager getNewOwlOntologyManagerInstance()
    {
        return OWLOntologyManagerFactoryRegistry.createOWLOntologyManager();
    }
    
    @Override
    protected PoddRepositoryManager getNewPoddRepositoryManagerInstance()
    {
        return new PoddRepositoryManagerImpl();
    }
    
}
