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
 * Tests the functionality of the PoddPurlProcessorFactoryRegistry.
 * 
 * @author kutila
 * 
 */
public class PoddPurlProcessorFactoryRegistryTest
{
    
    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception
    {
        // ensure the registry is clean of any factories that were loaded from the Environment
        PoddPurlProcessorFactoryRegistry.getInstance().clear();
        
        // create test Processor Factories
        final PoddPurlProcessorFactory factory4AllStages = Mockito.mock(PoddPurlProcessorFactory.class);
        Mockito.when(factory4AllStages.canHandleStage((PoddProcessorStage)Matchers.any())).thenReturn(true);
        Mockito.when(factory4AllStages.getKey()).thenReturn("key_ALL");
        
        final PoddPurlProcessorFactory factory4RDFParsingStage = Mockito.mock(PoddPurlProcessorFactory.class);
        Mockito.when(factory4RDFParsingStage.canHandleStage(PoddProcessorStage.RDF_PARSING)).thenReturn(true);
        Mockito.when(factory4RDFParsingStage.getKey()).thenReturn("key_RDF_PARSING");
        
        final PoddPurlProcessorFactory secondFactory4RDFParsingStage = Mockito.mock(PoddPurlProcessorFactory.class);
        Mockito.when(secondFactory4RDFParsingStage.canHandleStage(PoddProcessorStage.RDF_PARSING)).thenReturn(true);
        Mockito.when(secondFactory4RDFParsingStage.getKey()).thenReturn("key_RDF_PARSING");
        
        // add the mock Factories to the registry
        PoddPurlProcessorFactoryRegistry.getInstance().add(factory4AllStages);
        PoddPurlProcessorFactoryRegistry.getInstance().add(factory4RDFParsingStage);
        PoddPurlProcessorFactoryRegistry.getInstance().add(secondFactory4RDFParsingStage);
    }
    
    /**
     * 
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception
    {
        PoddPurlProcessorFactoryRegistry.getInstance().clear();
    }
    
    @Test
    public void testGetOnePerStage() throws Exception
    {
        final PoddPurlProcessorFactoryRegistry purlFactoryRegistry = PoddPurlProcessorFactoryRegistry.getInstance();
        final List<PoddPurlProcessorFactory> list = purlFactoryRegistry.getByStage(PoddProcessorStage.RDF_PARSING);
        Assert.assertEquals(3, list.size());
        Assert.assertEquals("key_RDF_PARSING", list.get(0).getKey());
        
        final List<PoddPurlProcessorFactory> list2 = purlFactoryRegistry.getByStage(PoddProcessorStage.PROFILE_CHECK);
        Assert.assertEquals(1, list2.size());
    }
}
