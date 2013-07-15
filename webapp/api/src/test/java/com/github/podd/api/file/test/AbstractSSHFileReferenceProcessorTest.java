/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.openrdf.model.URI;

import com.github.podd.api.file.DataReferenceProcessor;
import com.github.podd.api.file.SSHFileReference;
import com.github.podd.api.file.SSHFileReferenceProcessor;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 * 
 */
public abstract class AbstractSSHFileReferenceProcessorTest extends
        AbstractDataReferenceProcessorTest<SSHFileReference>
{
    @Override
    protected Set<URI> getExpectedDataReferenceTypes()
    {
        return Collections.singleton(PoddRdfConstants.PODD_BASE_FILE_REFERENCE_TYPE_SSH);
    }
    
    @Override
    protected final DataReferenceProcessor<SSHFileReference> getNewDataReferenceProcessor()
    {
        return this.getNewSSHFileReferenceProcessor();
    }
    
    protected abstract SSHFileReferenceProcessor getNewSSHFileReferenceProcessor();
    
    @Override
    protected String getPathToResourceWith2DataReferences()
    {
        return TestConstants.TEST_ARTIFACT_PURLS_2_FILE_REFS;
    }
    
    @Override
    protected void verify2DataReferences(final Collection<SSHFileReference> fileReferences)
    {
        Assert.assertNotNull("NULL collection of file references", fileReferences);
        Assert.assertEquals("Expected 2 file references to verify", 2, fileReferences.size());
        
        final List<String> objectIriList =
                Arrays.asList("http://purl.org/podd-test/130326f/object-rice-scan-34343-a",
                        "http://purl.org/podd-test/130326f/object-rice-scan-34343-b");
        
        final List<String> labelList = Arrays.asList("Rice tree scan 003454-98", "Rice tree scan 003454-99");
        final List<String> filenameList = Arrays.asList("plant_003456-233445.bag.zip", "plant_003456-233446.bag.zip");
        
        for(final SSHFileReference sshFileReference : fileReferences)
        {
            Assert.assertNull("Artifact ID should be NULL", sshFileReference.getArtifactID());
            Assert.assertEquals("http://purl.org/podd-test/130326f/objA24#SqueekeeMaterial", sshFileReference
                    .getParentIri().toString());
            Assert.assertTrue("File Reference URI is not an expected one",
                    objectIriList.contains(sshFileReference.getObjectIri().toString()));
            Assert.assertTrue("Label is not an expected one", labelList.contains(sshFileReference.getLabel()));
            Assert.assertTrue("File name is not an expected one", filenameList.contains(sshFileReference.getFilename()));
        }
    }
    
}
