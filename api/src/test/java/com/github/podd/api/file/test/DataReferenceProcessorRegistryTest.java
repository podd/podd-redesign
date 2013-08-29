/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.github.podd.api.PoddProcessorStage;
import com.github.podd.api.file.DataReferenceProcessorFactory;
import com.github.podd.api.file.DataReferenceProcessorRegistry;

/**
 * Tests functionality of the DataReferenceProcessorRegistry.
 * 
 * The test implementation is copied from {@link PoddPurlProcessorFactoryRegistryTest} with PURL
 * related references replaced by corresponding DataReference references.
 * 
 * @author kutila
 * 
 */
public class DataReferenceProcessorRegistryTest
{
    
    private DataReferenceProcessorRegistry testRegistry;
    
    private DataReferenceProcessorFactory factory4rdfParsingStage;
    private DataReferenceProcessorFactory secondFactory4RDFParsingStage;
    private DataReferenceProcessorFactory factory4AllStages;
    private DataReferenceProcessorFactory factory4InferenceStage;
    
    /**
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.testRegistry = new DataReferenceProcessorRegistry();
        this.testRegistry.clear();
        
        Assert.assertEquals("Registry wasn't cleared", 0, this.testRegistry.getAll().size());
        
        // create mock factories for use in tests
        this.factory4rdfParsingStage = Mockito.mock(DataReferenceProcessorFactory.class);
        Mockito.when(this.factory4rdfParsingStage.canHandleStage(PoddProcessorStage.RDF_PARSING)).thenReturn(true);
        Mockito.when(this.factory4rdfParsingStage.getKey()).thenReturn("key_RDF_PARSING");
        
        this.secondFactory4RDFParsingStage = Mockito.mock(DataReferenceProcessorFactory.class);
        Mockito.when(this.secondFactory4RDFParsingStage.canHandleStage(PoddProcessorStage.RDF_PARSING))
                .thenReturn(true);
        Mockito.when(this.secondFactory4RDFParsingStage.getKey()).thenReturn("key_RDF_PARSING");
        
        this.factory4InferenceStage = Mockito.mock(DataReferenceProcessorFactory.class);
        Mockito.when(this.factory4InferenceStage.canHandleStage(PoddProcessorStage.INFERENCE)).thenReturn(true);
        Mockito.when(this.factory4InferenceStage.getKey()).thenReturn("key_INFERENCE");
        
        this.factory4AllStages = Mockito.mock(DataReferenceProcessorFactory.class);
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
        final List<DataReferenceProcessorFactory> nullStageFactories = this.testRegistry.getByStage(null);
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
            final List<DataReferenceProcessorFactory> factories = this.testRegistry.getByStage(stage);
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
        final List<DataReferenceProcessorFactory> parsingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.RDF_PARSING);
        
        Assert.assertEquals(1, parsingStageFactories.size());
        Assert.assertEquals("key_RDF_PARSING", parsingStageFactories.get(0).getKey());
        
        // retrieve factories for PROFILE_CHECK stage
        final List<DataReferenceProcessorFactory> profileCheckingStageFactories =
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
        final List<DataReferenceProcessorFactory> parsingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.RDF_PARSING);
        
        Assert.assertEquals(1, parsingStageFactories.size());
        Assert.assertEquals("key_RDF_PARSING", parsingStageFactories.get(0).getKey());
        
        // retrieve factories for INFERENCE stage
        final List<DataReferenceProcessorFactory> inferenceStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.INFERENCE);
        
        Assert.assertEquals(1, inferenceStageFactories.size());
        Assert.assertEquals("key_INFERENCE", inferenceStageFactories.get(0).getKey());
        
        // retrieve factories for PROFILE_CHECK stage
        final List<DataReferenceProcessorFactory> profileCheckingStageFactories =
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
        final List<DataReferenceProcessorFactory> parsingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.RDF_PARSING);
        
        Assert.assertEquals(2, parsingStageFactories.size());
        Assert.assertEquals("key_RDF_PARSING", parsingStageFactories.get(0).getKey());
        
        // retrieve factories for PROFILE_CHECK stage
        final List<DataReferenceProcessorFactory> profileCheckingStageFactories =
                this.testRegistry.getByStage(PoddProcessorStage.PROFILE_CHECK);
        Assert.assertEquals(0, profileCheckingStageFactories.size());
    }
    
    @Test
    public void testGetInstance() throws Exception
    {
        Assert.assertNotNull("getInstance was null", DataReferenceProcessorRegistry.getInstance());
    }
    
}
