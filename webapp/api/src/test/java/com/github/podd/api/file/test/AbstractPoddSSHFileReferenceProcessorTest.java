/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.openrdf.model.URI;

import com.github.podd.api.file.PoddFileReferenceProcessor;
import com.github.podd.api.file.PoddSSHFileReference;
import com.github.podd.api.file.PoddSSHFileReferenceProcessor;
import com.github.podd.api.test.TestConstants;
import com.github.podd.utils.PoddRdfConstants;

/**
 * @author kutila
 *
 */
public abstract class AbstractPoddSSHFileReferenceProcessorTest extends AbstractPoddFileReferenceProcessorTest<PoddSSHFileReference>
{
    @Override
    protected final PoddFileReferenceProcessor<PoddSSHFileReference> getNewPoddFileReferenceProcessor()
    {
        return getNewPoddSSHFileReferenceProcessor();
    }

    protected abstract PoddSSHFileReferenceProcessor getNewPoddSSHFileReferenceProcessor();


    @Override
    protected void verify2FileReferences(Collection<PoddSSHFileReference> fileReferences)
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
