/**
 * 
 */
package com.github.podd.impl.test;

import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.test.AbstractPoddOWLManagerTest;
import com.github.podd.impl.PoddOWLManagerImpl;

/**
 * @author kutila
 * 
 */
public class PoddOWLManagerImplTest extends AbstractPoddOWLManagerTest
{
    private String reasonerName = "Pellet";
    
    @Override
    protected OWLReasonerFactory getNewOWLReasonerFactoryInstance()
    {
        return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory(this.reasonerName);
    }
    
    @Override
    protected PoddOWLManager getNewPoddOWLManagerInstance()
    {
        return new PoddOWLManagerImpl();
    }
    
}
