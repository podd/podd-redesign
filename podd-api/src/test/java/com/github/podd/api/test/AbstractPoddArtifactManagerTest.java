/**
 * 
 */
package com.github.podd.api.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Graph;

import com.github.podd.api.PoddArtifactManager;
import com.github.podd.api.PoddProcessorEvent;
import com.github.podd.api.PoddProcessorFactory;
import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.file.PoddFileReferenceProcessorFactory;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public abstract class AbstractPoddArtifactManagerTest
{
    protected abstract PoddArtifactManager getNewArtifactManager();
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        final PoddFileReferenceProcessorFactoryRegistry testRegistry = new PoddFileReferenceProcessorFactoryRegistry();
        
        final PoddArtifactManager testManager = this.getNewArtifactManager();
        
        for(final PoddProcessorStage nextStage : PoddProcessorStage.values())
        {
            for(final PoddFileReferenceProcessorFactory nextProcessor : testRegistry.getByStage(nextStage))
            {
                testManager.registerProcessor(nextProcessor, nextStage);
            }
        }
        
        final List<PoddProcessorFactory<?, ?, ?>> processors =
                testManager.getProcessors(PoddProcessorStage.RDF_PARSING);
        
        final PoddProcessorEvent<Graph> event = null;
        
        for(final PoddProcessorFactory<?, ?, ?> nextProcessor : processors)
        {
            // nextProcessor.getProcessor(event);
        }
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
