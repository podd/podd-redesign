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
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.file.FileReference;
import com.github.podd.api.file.FileReferenceManager;
import com.github.podd.api.file.FileReferenceProcessorFactoryRegistry;

/**
 * Abstract test class for FileReferenceManager.
 * 
 * @author kutila
 */
public abstract class AbstractFileReferenceManagerTest
{

    protected FileReferenceManager testFileReferenceManager;
    
    private FileReferenceProcessorFactoryRegistry testRegistry;
    
    private Repository testRepository;
    
    protected RepositoryConnection testRepositoryConnection;
    
    /**
     * @return A new FileReferenceManager instance for use by this test
     */
    public abstract FileReferenceManager getNewFileReferenceManager();
    
    /**
     * @return A new FileReferenceProcessorFactory Registry for use by this test
     */
    public abstract FileReferenceProcessorFactoryRegistry getNewPoddFileReferenceProcessorFactoryRegistry();
    
    @Before
    public void setUp() throws Exception
    {
        this.testRegistry = this.getNewPoddFileReferenceProcessorFactoryRegistry();
        Assert.assertNotNull("Null implementation of test Registry", this.testRegistry);
        
        this.testFileReferenceManager = this.getNewFileReferenceManager();
        Assert.assertNotNull("Null implementation of test FileReferenceManager", this.testFileReferenceManager);
        
        this.testRepository = new SailRepository(new MemoryStore());
        this.testRepository.initialize();
        
        this.testRepositoryConnection = this.testRepository.getConnection();
        this.testRepositoryConnection.begin(); // Transaction per each test
        
        this.testFileReferenceManager.setProcessorFactoryRegistry(this.testRegistry);
    }
    
    @After
    public void tearDown() throws Exception
    {
        this.testRegistry.clear();
        this.testRepositoryConnection.rollback();
        this.testRepositoryConnection.close();
        this.testRepository.shutDown();
        
        this.testFileReferenceManager = null;
    }
 
    @Test
    public void testExtractFileReferences() throws Exception
    {
        URI someContext = null;
        Set<FileReference> extractedFileReferences =
                this.testFileReferenceManager.extractFileReferences(testRepositoryConnection, someContext);
        // FIXME
    }
    
    // TODO add more tests
}
