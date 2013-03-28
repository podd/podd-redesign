/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

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
    protected PoddFileRepositoryManager testFileRepositoryManager;
    protected PoddRepositoryManager testRepositoryManager;
    
    /**
     * {@link PoddFileRepositoryManager} is the object under test in this class.
     * 
     * @return A new {@link PoddFileRepositoryManager} instance for use by the test
     */
    protected abstract PoddFileRepositoryManager getNewPoddFileRepositoryManager();
    
    /**
     * @return A new {@link PoddRepositoryManager} instance for use by the test
     */
    protected abstract PoddRepositoryManager getNewPoddRepositoryManager();
    
    @Before
    public void setUp() throws Exception
    {
        this.testFileRepositoryManager = this.getNewPoddFileRepositoryManager();
        this.testRepositoryManager = this.getNewPoddRepositoryManager();
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.testFileRepositoryManager = null;
        this.testRepositoryManager = null;
    }
    
    @Ignore
    @Test
    public void testAddRepositoryMapping() throws Exception
    {
        String alias = "TODO:some-alias";
        PoddFileRepository fileRepository = null;
        this.testFileRepositoryManager.addRepositoryMapping(alias, fileRepository);
        // TODO - implement me
    }
    
    @Ignore
    @Test
    public void testGetRepository() throws Exception
    {
        String alias = "TODO:some-alias";
        PoddFileRepository repository = this.testFileRepositoryManager.getRepository(alias);
        // TODO - implement me
    }
    
    @Test
    public void testGetRepositoryAlias() throws Exception
    {
        PoddFileRepository fileRepository = null;
        List<String> aliases = this.testFileRepositoryManager.getRepositoryAliases(fileRepository);
        // TODO - implement me
    }
    
    @Test
    public void testRemoveRepositoryMapping() throws Exception
    {
        String alias = "TODO:some-alias";
        PoddFileRepository removedRepositoryMapping = this.testFileRepositoryManager.removeRepositoryMapping(alias);
        // TODO - implement me
    }
    
}
