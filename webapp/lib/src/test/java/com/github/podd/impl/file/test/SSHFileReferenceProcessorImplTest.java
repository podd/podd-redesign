/**
 * 
 */
package com.github.podd.impl.file.test;

import java.io.InputStream;
import java.util.Collection;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFFormat;

import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddSSHFileReference;
import com.github.podd.api.file.test.AbstractPoddFileReferenceProcessorTest;
import com.github.podd.api.test.TestConstants;
import com.github.podd.impl.file.SSHFileReferenceProcessorImpl;
import com.github.podd.utils.PoddRdfConstants;
import com.github.podd.utils.RdfUtility;

/**
 * 
 * @author kutila
 */
public class SSHFileReferenceProcessorImplTest extends AbstractPoddFileReferenceProcessorTest<PoddSSHFileReference>
{

    @Override
    protected PoddFileReferenceProcessor<PoddSSHFileReference> getNewPoddFileReferenceProcessor()
    {
        return new SSHFileReferenceProcessorImpl();
    }
   
    @Override
    protected String getPathToResourceWithFileReferences()
    {
        return TestConstants.TEST_ARTIFACT_PURLS_2_FILE_REFS;
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
        Assert.assertEquals("Not the expected file reference type", PoddRdfConstants.PODDBASE_FILE_REFERENCE_TYPE_SSH,
                types.iterator().next());
    }

    /**
     * Detailed test of createReferences() which compares the extracted file references against expected values
     */
    @Test
    public void testCreateReferencesWithExpectedValues() throws Exception
    {
        InputStream resourceStream = this.getClass().getResourceAsStream(this.getPathToResourceWithFileReferences());
        Model model = RdfUtility.inputStreamToModel(resourceStream, RDFFormat.RDFXML);
        
        Collection<PoddSSHFileReference> references = this.fileReferenceProcessor.createReferences(model);
        
        Assert.assertNotNull("NULL collection of file references", references);
        Assert.assertFalse("No File references created", references.isEmpty());
        Assert.assertEquals("Not the expected number of file references", 2, references.size());
        
        //TODO - verify each of the file references generated
    }
    
}
