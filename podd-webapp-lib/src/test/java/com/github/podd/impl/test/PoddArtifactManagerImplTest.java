/**
 * 
 */
package com.github.podd.impl.test;

import java.io.InputStream;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactoryRegistry;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.PoddSesameManager;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.test.AbstractPoddArtifactManagerTest;
import com.github.podd.impl.PoddArtifactManagerImpl;
import com.github.podd.impl.PoddOWLManagerImpl;
import com.github.podd.impl.PoddRepositoryManagerImpl;
import com.github.podd.impl.PoddSchemaManagerImpl;
import com.github.podd.impl.PoddSesameManagerImpl;
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
    protected PoddSesameManager getNewSesameManager()
    {
        return new PoddSesameManagerImpl();
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
    
    @Test
    public void testGetOntologyIRI() throws Exception
    {
        final String resourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        
        final Repository testRepository = new SailRepository(new MemoryStore());
        testRepository.initialize();
        
        RepositoryConnection testRepositoryConnection = null;
        
        try
        {
            testRepositoryConnection = testRepository.getConnection();
            testRepositoryConnection.begin();
            
            testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
            
            final PoddArtifactManagerImpl testArtifactManager = new PoddArtifactManagerImpl();
            
            // invoke method under test:
            final IRI ontologyIRI =
                    testArtifactManager.getOntologyIRI(testRepositoryConnection, context);
            
            // verify:
            Assert.assertNotNull("Ontology IRI was null", ontologyIRI);
            Assert.assertEquals("Wrong Ontology IRI", "urn:temp:uuid:artifact:1", ontologyIRI.toString());
        }
        finally
        {
            testRepositoryConnection.rollback();
            testRepositoryConnection.close();
            testRepository.shutDown();
        }
    }
    
    @Test
    public void testGetDirectImports() throws Exception
    {
        final String resourcePath = "/test/artifacts/basicProject-1-internal-object.rdf";
        final URI context = ValueFactoryImpl.getInstance().createURI("urn:testcontext");
        
        final InputStream inputStream = this.getClass().getResourceAsStream(resourcePath);
        Assert.assertNotNull("Could not find resource", inputStream);
        
        final Repository testRepository = new SailRepository(new MemoryStore());
        testRepository.initialize();
        
        RepositoryConnection testRepositoryConnection = null;
        
        try
        {
            testRepositoryConnection = testRepository.getConnection();
            testRepositoryConnection.begin();
            
            testRepositoryConnection.add(inputStream, "", RDFFormat.RDFXML, context);
            
            final PoddArtifactManagerImpl testArtifactManager = new PoddArtifactManagerImpl();
            
            // invoke method under test:
            final Set<IRI> importedOntologyIRIs =
                    testArtifactManager.getDirectImports(testRepositoryConnection, context);
            
            // verify:
            Assert.assertNotNull("No imports could be found", importedOntologyIRIs);
            Assert.assertEquals("Incorrect number of imports found", 2, importedOntologyIRIs.size());
        }
        finally
        {
            testRepositoryConnection.rollback();
            testRepositoryConnection.close();
            testRepository.shutDown();
        }
    }
    
    @Test
    public void testIncrementVersion() throws Exception
    {
        String artifactURI = "http://some/artifact:15";

        final PoddArtifactManagerImpl testArtifactManager = new PoddArtifactManagerImpl();
        
        // increment the version number
        String newIncrementedVersion = testArtifactManager.incrementVersion(artifactURI + ":version:1");
        Assert.assertEquals("Version not incremented as expected", artifactURI + ":version:2", newIncrementedVersion);
        
        // append a number when version number cannot be extracted
        String newAppendedVersion = testArtifactManager.incrementVersion(artifactURI + ":v5");
        Assert.assertEquals("Version not incremented as expected", artifactURI + ":v51", newAppendedVersion);
    }
    
}
