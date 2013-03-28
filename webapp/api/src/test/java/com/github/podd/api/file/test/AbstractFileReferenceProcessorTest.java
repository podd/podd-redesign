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

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.FileReferenceProcessor;
import com.github.podd.api.file.SSHFileReference;
import com.github.podd.utils.RdfUtility;

/**
 * Abstract class to test FileReferenceProcessor sub-classes.
 * 
 * @author kutila
 * 
 */
public abstract class AbstractFileReferenceProcessorTest<T extends FileReference>
{
    protected FileReferenceProcessor<T> fileReferenceProcessor;
    
    /**
     * @return A new FileReferenceProcessor instance for use by the test
     */
    protected abstract FileReferenceProcessor<T> getNewFileReferenceProcessor();
    
    /**
     * @return The path to a Resource that has a set of RDF statements containing file references
     *         matching the Processor.
     */
    protected abstract String getPathToResourceWith2FileReferences();
    
    /**
     * 
     * @return A set of URIs that are expected as the file reference types for this processor.
     */
    protected abstract Set<URI> getExpectedFileReferenceTypes();
    
    protected abstract void verify2FileReferences(Collection<T> fileReferences);
    
    @Before
    public void setUp() throws Exception
    {
        this.fileReferenceProcessor = this.getNewFileReferenceProcessor();
        Assert.assertNotNull("Null implementation of test file reference processor", this.fileReferenceProcessor);
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.fileReferenceProcessor = null;
    }
    
    @Test
    public void testCanHandle() throws Exception
    {
        InputStream resourceStream = this.getClass().getResourceAsStream(this.getPathToResourceWith2FileReferences());
        Model model = RdfUtility.inputStreamToModel(resourceStream, RDFFormat.RDFXML);
        Assert.assertTrue("Expected to be able to handle this Model", fileReferenceProcessor.canHandle(model));
    }
    
    @Test
    public void testCanHandleWithEmptyModel() throws Exception
    {
        Model model = new LinkedHashModel();
        Assert.assertFalse("Should not be able to handle an empty model", this.fileReferenceProcessor.canHandle(model));
    }
    
    /**
     * Tests the behaviour when a null value is given to the canHandle() method
     */
    @Test
    public void testCanHandleWithNull() throws Exception
    {
        Assert.assertFalse("Should not be able to handle a NULL model", this.fileReferenceProcessor.canHandle(null));
    }
    
    /**
     * Verify that the Processor supports at least one type of file reference
     */
    @Test
    public void testGetTypes() throws Exception
    {
        Set<URI> types = this.fileReferenceProcessor.getTypes();
        Assert.assertFalse("No types found", types.isEmpty());
    }
    
    @Test
    public void testCreateReferences() throws Exception
    {
        InputStream resourceStream = this.getClass().getResourceAsStream(this.getPathToResourceWith2FileReferences());
        Model model = RdfUtility.inputStreamToModel(resourceStream, RDFFormat.RDFXML);
        
        Collection<T> references = this.fileReferenceProcessor.createReferences(model);
        
        Assert.assertNotNull("NULL collection of file references", references);
        Assert.assertFalse("No File references created", references.isEmpty());
        Assert.assertEquals("Not the expected number of file references", 2, references.size());
        
        verify2FileReferences(references);
    }
    
    @Test
    public void testCreateReferencesWithEmptyModel() throws Exception
    {
        Assert.assertNull("Expected NULL processing empty Model",
                this.fileReferenceProcessor.createReferences(new LinkedHashModel()));
    }
    
    @Test
    public void testCreateReferencesWithNull() throws Exception
    {
        Assert.assertNull("Expected NULL processing NULL Model", this.fileReferenceProcessor.createReferences(null));
    }
    
    /**
     * Detailed test of getTypes() comparing the number of supported types and the types themselves
     */
    @Test
    public void testGetTypesWithExpectedValues() throws Exception
    {
        Set<URI> types = this.fileReferenceProcessor.getTypes();
        Assert.assertFalse("No types found", types.isEmpty());
        Assert.assertEquals("Not the expected number of types", 1, types.size());
        for(URI nextExpectedType : getExpectedFileReferenceTypes())
        {
            Assert.assertTrue("Not the expected file reference type", types.contains(nextExpectedType));
        }
    }
    
    
}
