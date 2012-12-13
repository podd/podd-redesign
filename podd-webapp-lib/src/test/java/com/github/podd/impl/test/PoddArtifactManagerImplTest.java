/**
 * 
 */
package com.github.podd.impl.test;

import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.test.AbstractPoddArtifactManagerTest;
import com.github.podd.impl.PoddArtifactManagerImpl;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.file.PoddFileReferenceManagerImpl;
import com.github.podd.impl.purl.PoddPurlManagerImpl;
import com.github.podd.impl.purl.UUIDPurlProcessorFactoryImpl;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddArtifactManagerImplTest extends AbstractPoddArtifactManagerTest
{
    @Override
    protected PoddArtifactManager getNewArtifactManager()
    {
        return new PoddArtifactManagerImpl();
    }
    
    @Override
    protected PoddPurlProcessorFactory getNewDoiPurlProcessorFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected PoddFileReferenceManager getNewFileReferenceManager()
    {
        return new PoddFileReferenceManagerImpl();
    }
    
    @Override
    protected PoddPurlProcessorFactory getNewHandlePurlProcessorFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected PoddFileReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected PoddOWLManager getNewOWLManager()
    {
        return new PoddOWLManagerImpl();
    }
    
    @Override
    protected PoddPurlManager getNewPurlManager()
    {
        return new PoddPurlManagerImpl();
    }
    
    @Override
    protected OWLReasonerFactory getNewReasonerFactory()
    {
        return OWLReasonerFactoryRegistry.getInstance().getReasonerFactory("Pellet");
    }
    
    @Override
    protected PoddRepositoryManager getNewRepositoryManager() throws RepositoryException
    {
        return new PoddRepositoryManagerImpl();
    }
    
    @Override
    protected PoddSchemaManager getNewSchemaManager()
    {
        return new PoddSchemaManagerImpl();
    }
    
    @Override
    protected PoddFileReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory()
    {
        return new UUIDPurlProcessorFactoryImpl();
    }
}
