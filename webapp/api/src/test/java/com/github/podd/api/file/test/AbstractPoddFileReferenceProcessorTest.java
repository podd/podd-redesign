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

import com.github.podd.api.file.PoddFileReference;
import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.utils.RdfUtility;

/**
 * Abstract class to test PoddFileReferenceProcessor sub-classes.
 * 
 * @author kutila
 *
 */
public abstract class AbstractPoddFileReferenceProcessorTest<T extends PoddFileReference>
{
    protected PoddFileReferenceProcessor<T> fileReferenceProcessor;
    
    /**
     * @return A new PoddFileReferenceProcessor instance for use by the test
     */
    protected abstract PoddFileReferenceProcessor<T> getNewPoddFileReferenceProcessor();

    /**
     * @return The path to a Resource that has a set of RDF statements containing file references
     *         matching the Processor.
     */
    protected abstract String getPathToResourceWithFileReferences();
    
    @Before
    public void setUp() throws Exception
    {
        this.fileReferenceProcessor = this.getNewPoddFileReferenceProcessor();
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
        InputStream resourceStream = this.getClass().getResourceAsStream(this.getPathToResourceWithFileReferences());
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
        InputStream resourceStream = this.getClass().getResourceAsStream(this.getPathToResourceWithFileReferences());
        Model model = RdfUtility.inputStreamToModel(resourceStream, RDFFormat.RDFXML);
        
        Collection<T> references = this.fileReferenceProcessor.createReferences(model);
        
        Assert.assertNotNull("NULL collection of file references", references);
        Assert.assertFalse("No File references created", references.isEmpty());
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
    
}
