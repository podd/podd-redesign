/**
 * 
 */
package com.github.podd.api.file.test;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;

import com.github.podd.api.file.DataReference;
import com.github.podd.api.file.DataReferenceProcessor;
import com.github.podd.utils.RdfUtility;

/**
 * Abstract class to test DataReferenceProcessor sub-classes.
 * 
 * @author kutila
 * 
 */
public abstract class AbstractDataReferenceProcessorTest<T extends DataReference>
{
    protected DataReferenceProcessor<T> dataReferenceProcessor;
    
    /**
     * 
     * @return A set of URIs that are expected as the data reference types for this processor.
     */
    protected abstract Set<URI> getExpectedDataReferenceTypes();
    
    /**
     * @return A new DataReferenceProcessor instance for use by the test
     */
    protected abstract DataReferenceProcessor<T> getNewDataReferenceProcessor();
    
    /**
     * @return The path to a Resource that has a set of RDF statements containing data references
     *         matching the Processor.
     */
    protected abstract String getPathToResourceWith2DataReferences();
    
    @Before
    public void setUp() throws Exception
    {
        this.dataReferenceProcessor = this.getNewDataReferenceProcessor();
        Assert.assertNotNull("Null implementation of test data reference processor", this.dataReferenceProcessor);
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.dataReferenceProcessor = null;
    }
    
    @Test
    public void testCanHandle() throws Exception
    {
        final InputStream resourceStream =
                this.getClass().getResourceAsStream(this.getPathToResourceWith2DataReferences());
        final Model model = RdfUtility.inputStreamToModel(resourceStream, RDFFormat.RDFXML);
        Assert.assertTrue("Expected to be able to handle this Model", this.dataReferenceProcessor.canHandle(model));
    }
    
    @Test
    public void testCanHandleWithEmptyModel() throws Exception
    {
        final Model model = new LinkedHashModel();
        Assert.assertFalse("Should not be able to handle an empty model", this.dataReferenceProcessor.canHandle(model));
    }
    
    /**
     * Tests the behaviour when a null value is given to the canHandle() method
     */
    @Test
    public void testCanHandleWithNull() throws Exception
    {
        Assert.assertFalse("Should not be able to handle a NULL model", this.dataReferenceProcessor.canHandle(null));
    }
    
    @Test
    public void testCreateReferences() throws Exception
    {
        final InputStream resourceStream =
                this.getClass().getResourceAsStream(this.getPathToResourceWith2DataReferences());
        final Model model = RdfUtility.inputStreamToModel(resourceStream, RDFFormat.RDFXML);
        
        final Collection<T> references = this.dataReferenceProcessor.createReferences(model);
        
        Assert.assertNotNull("NULL collection of data references", references);
        Assert.assertFalse("No File references created", references.isEmpty());
        Assert.assertEquals("Not the expected number of data references", 2, references.size());
        
        this.verify2DataReferences(references);
    }
    
    @Test
    public void testCreateReferencesWithEmptyModel() throws Exception
    {
        Assert.assertNull("Expected NULL processing empty Model",
                this.dataReferenceProcessor.createReferences(new LinkedHashModel()));
    }
    
    @Test
    public void testCreateReferencesWithNull() throws Exception
    {
        Assert.assertNull("Expected NULL processing NULL Model", this.dataReferenceProcessor.createReferences(null));
    }
    
    /**
     * Verify that the Processor supports at least one type of data reference
     */
    @Test
    public void testGetTypes() throws Exception
    {
        final Set<URI> types = this.dataReferenceProcessor.getTypes();
        Assert.assertFalse("No types found", types.isEmpty());
    }
    
    /**
     * Detailed test of getTypes() comparing the number of supported types and the types themselves
     */
    @Test
    public void testGetTypesWithExpectedValues() throws Exception
    {
        final Set<URI> types = this.dataReferenceProcessor.getTypes();
        Assert.assertFalse("No types found", types.isEmpty());
        Assert.assertEquals("Not the expected number of types", 1, types.size());
        for(final URI nextExpectedType : this.getExpectedDataReferenceTypes())
        {
            Assert.assertTrue("Not the expected data reference type", types.contains(nextExpectedType));
        }
    }
    
    protected abstract void verify2DataReferences(Collection<T> dataReferences);
    
}
