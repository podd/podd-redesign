/**
 * 
 */
package com.github.podd.api.test;

import java.io.InputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.Rio;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddSchemaManager;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class AbstractPoddArtifactManagerTest
{
    
    protected abstract PoddArtifactManager getNewArtifactManager();
    
    protected abstract PoddPurlProcessorFactory getNewDoiPurlProcessorFactory();
    
    protected abstract PoddFileReferenceManager getNewFileReferenceManager();
    
    protected abstract PoddPurlProcessorFactory getNewHandlePurlProcessorFactory();
    
    protected abstract PoddFileReferenceProcessorFactory getNewHttpFileReferenceProcessorFactory();
    
    protected abstract PoddOWLManager getNewOWLManager();
    
    protected abstract PoddPurlManager getNewPurlManager();
    
    protected abstract OWLReasonerFactory getNewReasonerFactory();
    
    protected abstract PoddSchemaManager getNewSchemaManager();
    
    protected abstract PoddFileReferenceProcessorFactory getNewSSHFileReferenceProcessorFactory();
    
    protected abstract PoddPurlProcessorFactory getNewUUIDPurlProcessorFactory();
    
    /**
     * @throws java.lang.Exception
     */
    @SuppressWarnings("null")
    @Before
    public void setUp() throws Exception
    {
        // FIXME: This needs to be a constant
        final URI poddFileReferenceType =
                ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/ns/poddBase#PoddFileReference");
        
        final PoddFileReferenceProcessorFactoryRegistry testFileRegistry =
                new PoddFileReferenceProcessorFactoryRegistry();
        // clear any automatically added entries that may come from META-INF/services entries on the
        // classpath
        testFileRegistry.clear();
        // In practice, the following factories would be automatically added to the registry,
        // however for testing we want to explicitly add the ones we want to support for each test
        testFileRegistry.add(this.getNewSSHFileReferenceProcessorFactory());
        testFileRegistry.add(this.getNewHttpFileReferenceProcessorFactory());
        
        final PoddPurlProcessorFactoryRegistry testPurlRegistry = new PoddPurlProcessorFactoryRegistry();
        testPurlRegistry.clear();
        testPurlRegistry.add(this.getNewDoiPurlProcessorFactory());
        testPurlRegistry.add(this.getNewHandlePurlProcessorFactory());
        testPurlRegistry.add(this.getNewUUIDPurlProcessorFactory());
        
        final PoddFileReferenceManager testFileReferenceManager = this.getNewFileReferenceManager();
        testFileReferenceManager.setProcessorFactoryRegistry(testFileRegistry);
        
        final PoddPurlManager testPurlManager = this.getNewPurlManager();
        testPurlManager.setPurlProcessorFactoryRegistry(testPurlRegistry);
        
        final PoddOWLManager testOWLManager = this.getNewOWLManager();
        testOWLManager.setReasonerFactory(this.getNewReasonerFactory());
        
        final PoddArtifactManager testArtifactManager = this.getNewArtifactManager();
        testArtifactManager.setFileReferenceManager(testFileReferenceManager);
        testArtifactManager.setPurlManager(testPurlManager);
        testArtifactManager.setOwlManager(testOWLManager);
        
        final PoddSchemaManager testSchemaManager = this.getNewSchemaManager();
        testSchemaManager.setOwlManager(testOWLManager);
        
        final InputStream inputStream = this.getClass().getResourceAsStream("/testArtifact.rdf");
        // MIME type should be either given by the user, detected from the content type on the
        // request, or autodetected using the Any23 Mime Detector
        final String mimeType = "application/rdf+xml";
        final RDFFormat format = Rio.getParserFormatForMIMEType(mimeType, RDFFormat.RDFXML);
        
        final InferredOWLOntologyID resultArtifactId = testArtifactManager.loadArtifact(inputStream, format);
        
        // INSIDE the loadArtifact method...
        
    }
    
    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#deregisterProcessor(com.github.podd.api.PoddProcessorFactory, com.github.podd.api.PoddProcessorStage)}
     * .
     */
    @Test
    public final void testDeregisterProcessor()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#getProcessors(com.github.podd.api.PoddProcessorStage)}
     * .
     */
    @Test
    public final void testGetProcessors()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#loadArtifact(java.io.InputStream, org.openrdf.rio.RDFFormat)}
     * .
     */
    @Test
    public final void testLoadArtifact()
    {
        
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#publishArtifact(org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Test
    public final void testPublishArtifact()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#registerProcessor(com.github.podd.api.PoddProcessorFactory, com.github.podd.api.PoddProcessorStage)}
     * .
     */
    @Test
    public final void testRegisterProcessor()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
    /**
     * Test method for
     * {@link com.github.podd.api.PoddArtifactManager#updateSchemaImport(org.semanticweb.owlapi.model.OWLOntologyID, org.semanticweb.owlapi.model.OWLOntologyID)}
     * .
     */
    @Test
    public final void testUpdateSchemaImport()
    {
        Assert.fail("Not yet implemented"); // TODO
    }
    
}
