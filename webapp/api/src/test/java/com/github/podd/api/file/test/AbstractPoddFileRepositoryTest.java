/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.Model;
import org.openrdf.model.URI;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.exception.IncompleteFileRepositoryException;
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
    protected abstract PoddFileRepository<T> getNewPoddFileRepository() throws Exception;
    
    /**
     * @return A new {@link PoddFileRepository} instance for use by the test
     */
    protected abstract PoddFileRepository<T> getNewPoddFileRepository(final Model model) throws Exception;
    
    protected abstract T getNewFileReference(String alias);
    
    protected abstract List<Model> getIncompleteModels();
    
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
    public void testCanHandle() throws Exception
    {
        final String thisRepositorysAlias = this.testFileRepository.getAlias();
        final T fileReference = this.getNewFileReference(thisRepositorysAlias);
        
        Assert.assertTrue("Repository should be able to handle this file reference",
                this.testFileRepository.canHandle(fileReference));
    }
    
    @Test
    public void testCanHandleWithDifferentAliases() throws Exception
    {
        final T fileReference = this.getNewFileReference("wrong_alias");
        
        Assert.assertFalse("Repository should not be able to handle this file reference",
                this.testFileRepository.canHandle(fileReference));
    }
    
    @Test
    public void testCanHandleWithNullReference() throws Exception
    {
        Assert.assertFalse("Repository should not be able to handle NULL file reference",
                this.testFileRepository.canHandle(null));
    }
    
    @Test
    public void testCreateFileRepositoryWithIncompleteModel() throws Exception
    {
        
        final List<Model> incompleteModels = this.getIncompleteModels();
        
        for(final Model nextModel : incompleteModels)
        {
            try
            {
                this.getNewPoddFileRepository(nextModel);
                Assert.fail("Should have thrown an IncompleteFileRepositoryException");
            }
            catch(final IncompleteFileRepositoryException e)
            {
                Assert.assertNotNull(e.getModel());
                Assert.assertEquals("SSH repository configuration incomplete", e.getMessage());
            }
        }
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
