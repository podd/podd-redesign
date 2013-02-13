/**
 * 
 */
package com.github.podd.api.purl.test;

import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.purl.PoddPurlProcessorFactory;
import com.github.podd.api.purl.PoddPurlProcessorFactoryRegistry;

/**
 * Tests functionality of the PoddPurlProcessorFactoryRegistry.
 * 
 * @author kutila
 * 
 */
public class PoddPurlProcessorFactoryRegistryTest
{
    
    private PoddPurlProcessorFactoryRegistry testRegistry;
    
    private PoddPurlProcessorFactory factory4rdfParsingStage;
    private PoddPurlProcessorFactory secondFactory4RDFParsingStage;
    private PoddPurlProcessorFactory factory4AllStages;
    private PoddPurlProcessorFactory factory4InferenceStage;
    
    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testRegistry = new PoddPurlProcessorFactoryRegistry();
        this.testRegistry.clear();
        
        Assert.assertEquals("Registry wasn't cleared", 0, this.testRegistry.getAll().size());
        
        // create mock factories for use in tests
        this.factory4rdfParsingStage = Mockito.mock(PoddPurlProcessorFactory.class);
        Mockito.when(this.factory4rdfParsingStage.canHandleStage(PoddProcessorStage.RDF_PARSING)).thenReturn(true);
        Mockito.when(this.factory4rdfParsingStage.getKey()).thenReturn("key_RDF_PARSING");
        
        this.secondFactory4RDFParsingStage = Mockito.mock(PoddPurlProcessorFactory.class);
        Mockito.when(this.secondFactory4RDFParsingStage.canHandleStage(PoddProcessorStage.RDF_PARSING))
                .thenReturn(true);
        Mockito.when(this.secondFactory4RDFParsingStage.getKey()).thenReturn("key_RDF_PARSING");
        
        this.factory4InferenceStage = Mockito.mock(PoddPurlProcessorFactory.class);
        Mockito.when(this.factory4InferenceStage.canHandleStage(PoddProcessorStage.INFERENCE)).thenReturn(true);
        Mockito.when(this.factory4InferenceStage.getKey()).thenReturn("key_INFERENCE");
        
        this.factory4AllStages = Mockito.mock(PoddPurlProcessorFactory.class);
        Mockito.when(this.factory4AllStages.canHandleStage((PoddProcessorStage)Matchers.any())).thenReturn(true);
        Mockito.when(this.factory4AllStages.getKey()).thenReturn("key_ALL");
    }
    
    /**
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.testRegistry.clear();
        this.testRegistry = null;
    }
    
    @Test
    public void testGetByStageNullStage() throws Exception
    {
        final List<PoddPurlProcessorFactory> nullStageFactories = this.testRegistry.getByStage(null);
        Assert.assertNotNull(nullStageFactories);
        Assert.assertEquals("Should return an empty List for NULL stage", 0, nullStageFactories.size());
    }
    
    @Test
    public void testGetByStageOneFactoryMatchingAllStages() throws Exception
    {
        // add factories to Registry
        this.testRegistry.add(this.factory4AllStages);
        
        // go through ALL stages and verify the factory is returned for each one
        for(final PoddProcessorStage stage : PoddProcessorStage.values())
        {
            final List<PoddPurlProcessorFactory> factories = this.testRegistry.getByStage(stage);
            Assert.assertEquals(1, factories.size());
            Assert.assertEquals("key_ALL", factories.get(0).getKey());
        }
    }
    
    @Test
    public void testGetByStageOneFactoryMatchingOneStage() throws Exception
    {
        // add factories to Registry
        this.testRegistry.add(this.factory4rdfParsingStage);
        
        // retrieve factories for RDF_PARSING stage
        final List<PoddPurlProcessorFactory> parsingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.RDF_PARSING);
        
        Assert.assertEquals(1, parsingStageFactories.size());
        Assert.assertEquals("key_RDF_PARSING", parsingStageFactories.get(0).getKey());
        
        // retrieve factories for PROFILE_CHECK stage
        final List<PoddPurlProcessorFactory> profileCheckingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.PROFILE_CHECK);
        Assert.assertEquals(0, profileCheckingStageFactories.size());
    }
    
    @Test
    public void testGetByStageOneFactoryPerStage() throws Exception
    {
        // add factories to Registry
        this.testRegistry.add(this.factory4rdfParsingStage);
        this.testRegistry.add(this.factory4InferenceStage);
        
        // retrieve factories for RDF_PARSING stage
        final List<PoddPurlProcessorFactory> parsingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.RDF_PARSING);
        
        Assert.assertEquals(1, parsingStageFactories.size());
        Assert.assertEquals("key_RDF_PARSING", parsingStageFactories.get(0).getKey());
        
        // retrieve factories for INFERENCE stage
        final List<PoddPurlProcessorFactory> inferenceStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.INFERENCE);
        
        Assert.assertEquals(1, inferenceStageFactories.size());
        Assert.assertEquals("key_INFERENCE", inferenceStageFactories.get(0).getKey());
        
        // retrieve factories for PROFILE_CHECK stage
        final List<PoddPurlProcessorFactory> profileCheckingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.PROFILE_CHECK);
        Assert.assertEquals(0, profileCheckingStageFactories.size());
    }
    
    @Test
    public void testGetByStageTwoFactoriesMatchingOneStage() throws Exception
    {
        // add factories to Registry
        this.testRegistry.add(this.factory4rdfParsingStage);
        this.testRegistry.add(this.secondFactory4RDFParsingStage);
        
        // retrieve factories for RDF_PARSING stage
        final List<PoddPurlProcessorFactory> parsingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.RDF_PARSING);
        
        Assert.assertEquals(2, parsingStageFactories.size());
        Assert.assertEquals("key_RDF_PARSING", parsingStageFactories.get(0).getKey());
        
        // retrieve factories for PROFILE_CHECK stage
        final List<PoddPurlProcessorFactory> profileCheckingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.PROFILE_CHECK);
        Assert.assertEquals(0, profileCheckingStageFactories.size());
    }
    
    @Test
    public void testGetInstance() throws Exception
    {
        Assert.assertNotNull("getInstance was null", PoddPurlProcessorFactoryRegistry.getInstance());
    }
    
}
