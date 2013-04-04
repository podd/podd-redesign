/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddFileRepositoryManager;

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
        Repository repository = this.testRepositoryManager.getRepository();
        repository.shutDown();
        
        this.testFileRepositoryManager = null;
        this.testRepositoryManager = null;
    }
    
    @Test
    public void testAddRepositoryMappingWithNullRepository() throws Exception
    {
        String alias = "TODO:some-alias";
        PoddFileRepository<?> fileRepository = null;
        try
        {
            this.testFileRepositoryManager.addRepositoryMapping(alias, fileRepository);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch (NullPointerException e)
        {
            Assert.assertEquals("Not the expected error message", "Cannot add NULL as a File Repository mapping", e.getMessage());
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
        String alias = "TODO:some-alias";
        PoddFileRepository<?> fileRepository = null;
        this.testFileRepositoryManager.addRepositoryMapping(alias, fileRepository);
        // TODO - implement me
    }
    
    @Test
    public void testGetRepositoryWithNonExistentAlias() throws Exception
    {
        PoddFileRepository<?> repository = this.testFileRepositoryManager.getRepository("no_such_alias");
        Assert.assertNull("FileRepository should be NULL", repository);
    }
    
    @Ignore
    @Test
    public void testGetRepository() throws Exception
    {
        PoddFileRepository<?> repository = this.testFileRepositoryManager.getRepository(TEST_ALIAS_1A);
        Assert.assertNotNull("FileRepository was NULL", repository);
        Assert.assertEquals("Not the expected alias", TEST_ALIAS_1A, repository.getAlias());
        //Assert.assertTrue("", repository.getTypes().contains(PoddRdfConstants.PODD_SSH_FILE_REPOSITORY));
    }
    @Ignore
    @Test
    public void testGetRepositoryAlias() throws Exception
    {
        PoddFileRepository<?> fileRepository = null;
        List<String> aliases = this.testFileRepositoryManager.getRepositoryAliases(fileRepository);
        // TODO - implement me
    }
    
    @Ignore
    @Test
    public void testRemoveRepositoryMapping() throws Exception
    {
        PoddFileRepository<?> removedRepositoryMapping = this.testFileRepositoryManager.removeRepositoryMapping(TEST_ALIAS_1A);
        // TODO - implement me
    }
    
}
