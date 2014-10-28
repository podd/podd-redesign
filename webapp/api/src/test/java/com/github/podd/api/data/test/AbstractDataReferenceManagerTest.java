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
package com.github.podd.api.data.test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.data.DataReference;
import com.github.podd.api.data.DataReferenceManager;
import com.github.podd.api.data.DataReferenceProcessorRegistry;
import com.github.podd.api.test.TestConstants;

/**
 * Abstract test class for DataReferenceManager.
 *
 * @author kutila
 */
public abstract class AbstractDataReferenceManagerTest
{
    
    protected DataReferenceManager testDataReferenceManager;
    
    private DataReferenceProcessorRegistry testRegistry;
    
    private Repository testRepository;
    
    protected RepositoryConnection testRepositoryConnection;
    
    /**
     * @return A new DataReferenceManager instance for use by this test
     */
    public abstract DataReferenceManager getNewDataReferenceManager();
    
    /**
     * @return A new DataReferenceProcessorFactory Registry for use by this test
     */
    public abstract DataReferenceProcessorRegistry getNewDataReferenceProcessorRegistry();
    
    @Before
    public void setUp() throws Exception
    {
        this.testRegistry = this.getNewDataReferenceProcessorRegistry();
        Assert.assertNotNull("Null implementation of test Registry", this.testRegistry);
        
        this.testDataReferenceManager = this.getNewDataReferenceManager();
        Assert.assertNotNull("Null implementation of test DataReferenceManager", this.testDataReferenceManager);
        
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        this.testRepositoryConnection = this.testRepository.getConnection();
        // this.testRepositoryConnection.begin(); // Transaction per each test
        
        this.testDataReferenceManager.setDataProcessorRegistry(this.testRegistry);
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.testRegistry.clear();
        // this.testRepositoryConnection.rollback();
        this.testRepositoryConnection.close();
        this.testRepository.shutDown();
        
        this.testDataReferenceManager = null;
    }
    
    @Test
    public void testExtractFileReferencesFromEmptyRepository() throws Exception
    {
        final URI randomContext =
                ValueFactoryImpl.getInstance().createURI("urn:random:" + UUID.randomUUID().toString());
        
        final Set<DataReference> extractedFileReferences =
                this.testDataReferenceManager.extractDataReferences(this.testRepositoryConnection, randomContext);
        Assert.assertTrue("Should not have found any file references", extractedFileReferences.isEmpty());
    }
    
    @Test
    public void testExtractFileReferencesFromRepositoryWith1FileRef() throws Exception
    {
        final URI randomContext =
                ValueFactoryImpl.getInstance().createURI("urn:random:" + UUID.randomUUID().toString());
        final InputStream resourceStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_FRAGMENT_NEW_FILE_REF_OBJECT);
        this.testRepositoryConnection.add(resourceStream, "", RDFFormat.RDFXML, randomContext);
        
        final Set<DataReference> extractedFileReferences =
                this.testDataReferenceManager.extractDataReferences(this.testRepositoryConnection, randomContext);
        
        // verify:
        Assert.assertFalse("Could not find file references", extractedFileReferences.isEmpty());
        Assert.assertEquals("Not the expected number of file references", 1, extractedFileReferences.size());
        final DataReference dataReference = extractedFileReferences.iterator().next();
        Assert.assertNull("Artifact ID should be NULL", dataReference.getArtifactID());
        Assert.assertEquals("Not the expected Parent IRI",
                "http://purl.org/podd/basic-2-20130206/artifact:1#publication45", dataReference.getParentIri()
                        .toString());
        Assert.assertEquals("Not the expected IRI", "urn:temp:uuid:object-rice-scan-34343-a", dataReference
                .getObjectIri().toString());
        Assert.assertEquals("Not the expected label", "Rice tree scan 003454-98", dataReference.getLabel());
        Assert.assertEquals("Not the expected alias", "csiro_dap", dataReference.getRepositoryAlias());
    }
    
    @Test
    public void testExtractFileReferencesFromRepositoryWith2FileRefs() throws Exception
    {
        // populate Repository with a test artifact
        final URI randomContext =
                ValueFactoryImpl.getInstance().createURI("urn:random:" + UUID.randomUUID().toString());
        final InputStream resourceStream =
                this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_PURLS_2_FILE_REFS);
        this.testRepositoryConnection.add(resourceStream, "", RDFFormat.RDFXML, randomContext);
        
        final Set<DataReference> extractedFileReferences =
                this.testDataReferenceManager.extractDataReferences(this.testRepositoryConnection, randomContext);
        
        // verify:
        Assert.assertNotNull("NULL extracted references", extractedFileReferences);
        Assert.assertEquals("Not the expected number of file references", 2, extractedFileReferences.size());
        
        final List<String> expectedObjectIris =
                Arrays.asList("http://purl.org/podd-test/130326f/object-rice-scan-34343-a",
                        "http://purl.org/podd-test/130326f/object-rice-scan-34343-b");
        final List<String> expectedLabels = Arrays.asList("Rice tree scan 003454-98", "Rice tree scan 003454-99");
        final List<String> expectedAliases = Arrays.asList("csiro_dap");
        
        for(final DataReference dataReference : extractedFileReferences)
        {
            Assert.assertNull("Artifact ID should be NULL", dataReference.getArtifactID());
            Assert.assertEquals("Parent IRI is not as expected",
                    "http://purl.org/podd-test/130326f/objA24#SqueekeeMaterial", dataReference.getParentIri()
                            .toString());
            Assert.assertTrue("File Reference URI is not an expected one",
                    expectedObjectIris.contains(dataReference.getObjectIri().toString()));
            Assert.assertTrue("Label is not an expected one", expectedLabels.contains(dataReference.getLabel()));
            Assert.assertTrue("Alias is not an expected one",
                    expectedAliases.contains(dataReference.getRepositoryAlias()));
        }
        
    }
    
    @Test
    public void testExtractFileReferencesFromRepositoryWithoutFileReferences() throws Exception
    {
        final URI randomContext =
                ValueFactoryImpl.getInstance().createURI("urn:random:" + UUID.randomUUID().toString());
        final InputStream resourceStream = this.getClass().getResourceAsStream(TestConstants.TEST_ARTIFACT_20130206);
        this.testRepositoryConnection.add(resourceStream, "", RDFFormat.TURTLE, randomContext);
        
        final Set<DataReference> extractedFileReferences =
                this.testDataReferenceManager.extractDataReferences(this.testRepositoryConnection, randomContext);
        Assert.assertTrue("Should not have found any file references", extractedFileReferences.isEmpty());
    }
    
    // TODO add more tests
}
