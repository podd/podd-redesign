/**
 * 
 */
package com.github.podd.impl.test;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.file.DataReferenceManager;
import com.github.podd.api.file.DataReferenceProcessorFactory;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.test.AbstractPoddArtifactManagerTest;
import com.github.podd.impl.PoddArtifactManagerImpl;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
import com.github.podd.impl.file.FileReferenceManagerImpl;
import com.github.podd.impl.file.SSHFileReferenceProcessorFactoryImpl;
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
    protected DataReferenceManager getNewFileReferenceManager()
    {
        return new FileReferenceManagerImpl();
    }
    
    @Override
    protected PoddPurlProcessorFactory getNewHandlePurlProcessorFactory()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    protected DataReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory()
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
    protected PoddSesameManager getNewSesameManager()
    {
        return new PoddSesameManagerImpl();
    }
    
    @Override
    protected DataReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory()
    {
        return new SSHFileReferenceProcessorFactoryImpl();
    }
    
    @Override
    protected PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory()
    {
        return new UUIDPurlProcessorFactoryImpl();
    }
    
    @Test
    public void testIncrementVersion() throws Exception
    {
        final String artifactURI = "http://some/artifact:15";
        
        final PoddArtifactManagerImpl testArtifactManager = new PoddArtifactManagerImpl();
        
        // increment the version number
        final String newIncrementedVersion = testArtifactManager.incrementVersion(artifactURI + ":version:1");
        Assert.assertEquals("Version not incremented as expected", artifactURI + ":version:2", newIncrementedVersion);
        
        // append a number when version number cannot be extracted
        final String newAppendedVersion = testArtifactManager.incrementVersion(artifactURI + ":v5");
        Assert.assertEquals("Version not incremented as expected", artifactURI + ":v51", newAppendedVersion);
    }
    
}
