/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.utils.PoddRdfConstants;

/**
 * 
 * @author kutila
 */
public abstract class AbstractPoddFileRepositoryTest<T extends FileReference>
{
    protected PoddFileRepository<T> testFileRepository;
    
    /**
     * @return A new {@link PoddFileRepository} instance for use by the test
     */
    protected abstract PoddFileRepository<T> getNewPoddFileRepository();
    
    @Before
    public void setUp() throws Exception
    {
        this.testFileRepository = this.getNewPoddFileRepository();
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.testFileRepository = null;
    }
    
    @Test
    public void testGetAlias() throws Exception
    {
        final String alias = this.testFileRepository.getAlias();
        Assert.assertNotNull("NULL alias", alias);
    }
    
    @Test
    public void testGetTypes() throws Exception
    {
        final Set<URI> types = this.testFileRepository.getTypes();
        Assert.assertNotNull("NULL types", types);
        Assert.assertEquals("Expected 2 TYPEs", 2, types.size());
        Assert.assertTrue("Expected TYPE missing", types.contains(PoddRdfConstants.PODD_FILE_REPOSITORY));
    }
    
}
