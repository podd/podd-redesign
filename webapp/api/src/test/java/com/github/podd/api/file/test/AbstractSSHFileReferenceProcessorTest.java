/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.openrdf.model.URI;

import com.github.podd.api.file.FileReferenceProcessor;
import com.github.podd.api.file.SSHFileReference;
import com.github.podd.api.file.SSHFileReferenceProcessor;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 *
 */
public abstract class AbstractSSHFileReferenceProcessorTest extends AbstractFileReferenceProcessorTest<SSHFileReference>
{
    @Override
    protected final FileReferenceProcessor<SSHFileReference> getNewFileReferenceProcessor()
    {
        return getNewSSHFileReferenceProcessor();
    }

    protected abstract SSHFileReferenceProcessor getNewSSHFileReferenceProcessor();


    @Override
    protected void verify2FileReferences(Collection<SSHFileReference> fileReferences)
    {
        // TODO Auto-generated method stub
        
    }
    
    @Override
    protected String getPathToResourceWith2FileReferences()
    {
        return TestConstants.TEST_ARTIFACT_PURLS_2_FILE_REFS;
    }
    
    @Override
    protected Set<URI> getExpectedFileReferenceTypes()
    {
        return Collections.singleton(PoddRdfConstants.PODDBASE_FILE_REFERENCE_TYPE_SSH);
    }
    
}
