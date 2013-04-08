/**
 * 
 */
package com.github.podd.api.file.test;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.api.file.PoddFileRepositoryManager;
import com.github.podd.exception.FileRepositoryException;
import com.github.podd.exception.FileRepositoryMappingNotFoundException;

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
     * Build a File Repository object in memory with the given alias and Model. ALl other method
     * implementations may be empty.
     * 
     * @param alias
     *            The alias to be assigned to this object
     * @param model
     *            A Model containing implementation-specific configurations for this object
     * @return A valid PoddFileRepository instance
     */
    protected abstract PoddFileRepository<?> buildFileRepositoryInstance(final String alias, final Model model);
    
    /**
     * Create a {@link Model} that can be used to construct a File Repository. Implementation
     * specific default values should be set by this method such that the returned {@link Model} is
     * complete.
     * 
     * @param aliasUri
     *            The URI to use as the subject in the Model
     * @param aliases
     *            The aliases which should be set in the Model
     * @return
     */
    protected abstract Model buildModelForFileRepository(final URI aliasUri, final String... aliases);
    
    /**
     * {@link PoddFileRepositoryManager} is the object under test in this class.
     * 
     * @return A new {@link PoddFileRepositoryManager} instance for use by the test
     * @throws OpenRDFException
     */
    protected abstract PoddFileRepositoryManager getNewPoddFileRepositoryManager() throws OpenRDFException;
    
    @Before
    public void setUp() throws Exception
    {
        this.testFileRepositoryManager = this.getNewPoddFileRepositoryManager();
        this.testRepositoryManager = this.testFileRepositoryManager.getRepositoryManager();
        
        final RepositoryConnection conn = this.testRepositoryManager.getRepository().getConnection();
        try
        {
            conn.begin();
            
            final URI context = this.testRepositoryManager.getFileRepositoryManagementGraph();
            
            // repository configuration with 1 mapped alias
            final URI alias1Uri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/alias/1-alpha");
            final Model model1 =
                    this.buildModelForFileRepository(alias1Uri, AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A);
            conn.add(model1, context);
            
            // repository configuration with 2 mapped aliases
            final URI alias2Uri = ValueFactoryImpl.getInstance().createURI("http://purl.org/podd/alias/2-beta");
            final Model model2 =
                    this.buildModelForFileRepository(alias2Uri, AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A,
                            AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2B);
            conn.add(model2, context);
            
            conn.commit();
        }
        finally
        {
            conn.close();
        }
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
    public void testAddRepositoryMappingToExistingRepositoryConfiguration() throws Exception
    {
        // prepare:
        final String alias = "alias-1-gamma";
        final PoddFileRepository<?> existingFileRepository =
                this.testFileRepositoryManager.getRepository(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A);
        Assert.assertNotNull("FileRepository was NULL", existingFileRepository);
        
        this.testFileRepositoryManager.addRepositoryMapping(alias, existingFileRepository);
        
        // verify:
        final PoddFileRepository<?> mappedRepository = this.testFileRepositoryManager.getRepository(alias);
        Assert.assertNotNull("Alias mapping returns NULL", mappedRepository);
    }
    
    @Test
    public void testAddRepositoryMappingWithNewRepositoryConfiguration() throws Exception
    {
        // prepare:
        final String alias = "alias-1-gamma";
        final URI aliasUri = ValueFactoryImpl.getInstance().createURI("http://purl.org/alias-1-gamma");
        final PoddFileRepository<?> fileRepository =
                this.buildFileRepositoryInstance(alias, this.buildModelForFileRepository(aliasUri, alias));
        
        Assert.assertNotNull("FileRepository was NULL", fileRepository);
        
        this.testFileRepositoryManager.addRepositoryMapping(alias, fileRepository);
        
        // verify:
        final PoddFileRepository<?> mappedRepository = this.testFileRepositoryManager.getRepository(alias);
        Assert.assertNotNull("Alias mapping returns NULL", mappedRepository);
    }
    
    @Test
    public void testAddRepositoryMappingWithNullAlias() throws Exception
    {
        final PoddFileRepository<?> fileRepository =
                this.buildFileRepositoryInstance(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A, null);
        try
        {
            this.testFileRepositoryManager.addRepositoryMapping(null, fileRepository);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected error message", "Cannot add NULL as a File Repository mapping",
                    e.getMessage());
        }
    }
    
    @Test
    public void testAddRepositoryMappingWithNullRepository() throws Exception
    {
        final PoddFileRepository<?> fileRepository = null;
        try
        {
            this.testFileRepositoryManager.addRepositoryMapping(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A,
                    fileRepository);
            Assert.fail("Should have thrown a NullPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertEquals("Not the expected error message", "Cannot add NULL as a File Repository mapping",
                    e.getMessage());
        }
    }
    
    @Test
    public void testGetRepositoryAliasesSuccess() throws Exception
    {
        final PoddFileRepository<?> fileRepository =
                this.buildFileRepositoryInstance(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A, null);
        
        final List<String> aliases = this.testFileRepositoryManager.getRepositoryAliases(fileRepository);
        
        // verify:
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Not the expected number of aliases", 2, aliases.size());
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A));
        Assert.assertTrue("Expected alias missing",
                aliases.contains(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2B));
    }
    
    @Test
    public void testGetRepositoryAliasesWithNonExistentRepository() throws Exception
    {
        final PoddFileRepository<?> fileRepository = this.buildFileRepositoryInstance("no_such_alias", null);
        
        final List<String> aliases = this.testFileRepositoryManager.getRepositoryAliases(fileRepository);
        
        // verify:
        Assert.assertNotNull("NULL list of aliases", aliases);
        Assert.assertEquals("Expected no aliases", 0, aliases.size());
    }
    
    @Test
    public void testGetRepositoryAliasesWithNullFileRepository() throws Exception
    {
        try
        {
            this.testFileRepositoryManager.getRepositoryAliases((PoddFileRepository<?>)null);
            Assert.fail("Should have thrown a NULLPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testGetRepositoryHavingMultipleAliases() throws Exception
    {
        final PoddFileRepository<?> repository =
                this.testFileRepositoryManager.getRepository(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A);
        
        Assert.assertNotNull("FileRepository was NULL", repository);
        Assert.assertEquals("Not the expected alias", AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A,
                repository.getAlias());
        Assert.assertNotNull("FileRepository has NULL types", repository.getTypes());
        Assert.assertEquals("Not the expected no. of types", 2, repository.getTypes().size());
    }
    
    @Test
    public void testGetRepositoryHavingSingleAlias() throws Exception
    {
        final PoddFileRepository<?> repository =
                this.testFileRepositoryManager.getRepository(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A);
        
        Assert.assertNotNull("FileRepository was NULL", repository);
        Assert.assertEquals("Not the expected alias", AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A,
                repository.getAlias());
        Assert.assertNotNull("FileRepository has NULL types", repository.getTypes());
        Assert.assertEquals("Not the expected no. of types", 2, repository.getTypes().size());
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
    public void testRemoveRepositoryMappingOnlyAlias() throws Exception
    {
        final PoddFileRepository<?> removedRepositoryMapping =
                this.testFileRepositoryManager
                        .removeRepositoryMapping(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A);
        
        // verify:
        Assert.assertNotNull("Removed Mapping is NULL", removedRepositoryMapping);
        Assert.assertEquals("Removed alias is incorrect", AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A,
                removedRepositoryMapping.getAlias());
        Assert.assertNull("A mapping still exists for this Alias",
                this.testFileRepositoryManager.getRepository(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_1A));
    }
    
    @Test
    public void testRemoveRepositoryMappingWhenMultipleAliasesExist() throws Exception
    {
        // prepare:
        final List<String> existingAliases =
                this.testFileRepositoryManager
                        .getRepositoryAliases(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A);
        Assert.assertEquals("Test setup should have 2 aliases mapped", 2, existingAliases.size());
        existingAliases.remove(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A);
        
        final PoddFileRepository<?> removedRepositoryMapping =
                this.testFileRepositoryManager
                        .removeRepositoryMapping(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A);
        
        // verify:
        Assert.assertNotNull("Removed Mapping is NULL", removedRepositoryMapping);
        Assert.assertEquals("Removed alias is incorrect", AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A,
                removedRepositoryMapping.getAlias());
        Assert.assertNull("A mapping still exists for this Alias",
                this.testFileRepositoryManager.getRepository(AbstractPoddFileRepositoryManagerTest.TEST_ALIAS_2A));
        
        for(final String existingAlias : existingAliases)
        {
            Assert.assertNotNull("The other alias should still be mapped",
                    this.testFileRepositoryManager.getRepository(existingAlias));
        }
    }
    
    @Test
    public void testRemoveRepositoryMappingWithNullAlias() throws Exception
    {
        try
        {
            this.testFileRepositoryManager.removeRepositoryMapping(null);
            Assert.fail("Should have thrown a NULLPointerException");
        }
        catch(final NullPointerException e)
        {
            Assert.assertNotNull(e);
        }
    }
    
    @Test
    public void testRemoveRepositoryWithNonExistentAlias() throws Exception
    {
        try
        {
            this.testFileRepositoryManager.removeRepositoryMapping("no_such_alias");
            Assert.fail("Should have thrown a NULLPointerException");
        }
        catch(final FileRepositoryException e)
        {
            Assert.assertTrue(e instanceof FileRepositoryMappingNotFoundException);
        }
    }
    
}
