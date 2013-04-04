/**
 * 
 */
package com.github.podd.api.file.test;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.repository.Repository;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.exception.FileReferenceNotSupportedException;

/**
 * Abstract test to verify that the PoddFileRepositoryManager API contract is followed by
 * implementations.
 * 
 * @author kutila
 */
public abstract class AbstractPoddFileRepositoryManagerTest
{
    public static final String TEST_ALIAS_1A = "alias-1-alpha";
    public static final String TEST_ALIAS_1B = "alias-1-beta";
    public static final String TEST_ALIAS_2A = "alias-2-alpha";
    public static final String TEST_ALIAS_2B = "alias-2-beta";
    
    protected PoddFileRepositoryManager testFileRepositoryManager;
    protected PoddRepositoryManager testRepositoryManager;
    
    /**
     * {@link PoddFileRepositoryManager} is the object under test in this class.
     * 
     * @return A new {@link PoddFileRepositoryManager} instance for use by the test
     * @throws OpenRDFException
     */
    protected abstract PoddFileRepositoryManager getNewPoddFileRepositoryManager() throws OpenRDFException;
    
    protected abstract void populateFileRepositoryManagementGraph() throws OpenRDFException;
    
    @Before
    public void setUp() throws Exception
    {
        this.testFileRepositoryManager = this.getNewPoddFileRepositoryManager();
        this.testRepositoryManager = this.testFileRepositoryManager.getRepositoryManager();
        
        this.populateFileRepositoryManagementGraph();
    }
    
    @After
    public void tearDown() throws Exception
    {
        final Repository repository = this.testRepositoryManager.getRepository();
        repository.shutDown();
        
        this.testFileRepositoryManager = null;
        this.testRepositoryManager = null;
    }
    
    @Test
    public void testAddRepositoryMappingWithNullRepository() throws Exception
    {
        final String alias = "TODO:some-alias";
        final PoddFileRepository<?> fileRepository = null;
        try
        {
            this.testFileRepositoryManager.addRepositoryMapping(alias, fileRepository);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected error message", "Cannot add NULL as a File Repository mapping",
                    e.getMessage());
        }
    }
    
    @Ignore
    @Test
    public void testAddRepositoryMappingWithNullAlias() throws Exception
    {
        // TODO - implement me
    }
    
    @Ignore
    @Test
    public void testAddRepositoryMapping() throws Exception
    {
        final String alias = "TODO:some-alias";
        final PoddFileRepository<?> fileRepository = null;
        this.testFileRepositoryManager.addRepositoryMapping(alias, fileRepository);
        // TODO - implement me
    }
    
    @Test
    public void testGetRepositorySuccess() throws Exception
    {
        final PoddFileRepository<?> repository =
                this.testFileRepositoryManager.getRepository(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A);
        Assert.assertNotNull("FileRepository was NULL", repository);
        Assert.assertEquals("Not the expected alias", AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A,
                repository.getAlias());
    }
    
    @Test
    public void testGetRepositoryWithNonExistentAlias() throws Exception
    {
        final PoddFileRepository<?> repository = this.testFileRepositoryManager.getRepository("no_such_alias");
        Assert.assertNull("FileRepository should be NULL", repository);
    }
    
    @Test
    public void testGetRepositoryWithNullAlias() throws Exception
    {
        final PoddFileRepository<?> repository = this.testFileRepositoryManager.getRepository(null);
        Assert.assertNull("FileRepository should be NULL", repository);
    }
    
    @Test
    public void testGetRepositoryAliases() throws Exception
    {
        // create a mock PoddFileRepository which can only return the test alias string
        final PoddFileRepository<?> fileRepository = new PoddFileRepository<FileReference>()
            {
                
                @Override
                public String getAlias()
                {
                    return AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A;
                }
                
                @Override
                public Set getTypes()
                {
                    return null;
                }
                
                @Override
                public boolean validate(final FileReference reference) throws FileReferenceNotSupportedException,
                    IOException
                {
                    return false;
                }
                
                @Override
                public boolean canHandle(final FileReference reference)
                {
                    return false;
                }
                
                @Override
                public Model getAsModel()
                {
                    return null;
                }
            };
        
        final List<String> aliases = this.testFileRepositoryManager.getRepositoryAliases(fileRepository);
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Not the expected number of aliases", 2, aliases.size());
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A));
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2B));
    }
    
    @Test
    public void testGetRepositoryAliasesWithNullFileRepository() throws Exception
    {
        try
        {
            this.testFileRepositoryManager.getRepositoryAliases(null);
            Assert.fail("Should have thrown a NULLPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    @Ignore
    @Test
    public void testRemoveRepositoryMapping() throws Exception
    {
        final PoddFileRepository<?> removedRepositoryMapping =
                this.testFileRepositoryManager
                        .removeRepositoryMapping(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A);
        // TODO - implement me
    }
    
}
