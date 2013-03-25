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

import com.github.podd.api.file.PoddFileReference;
import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.file.PoddFileReferenceProcessorFactoryRegistry;

/**
 * Abstract test class for PoddFileReferenceManager.
 * 
 * @author kutila
 */
public abstract class AbstractPoddFileReferenceManagerTest
{

    protected PoddFileReferenceManager testFileReferenceManager;
    
    private PoddFileReferenceProcessorFactoryRegistry testRegistry;
    
    private Repository testRepository;
    
    protected RepositoryConnection testRepositoryConnection;
    
    /**
     * @return A new PoddFileReferenceManager instance for use by this test
     */
    public abstract PoddFileReferenceManager getNewPoddFileReferenceManager();
    
    /**
     * @return A new FileReferenceProcessorFactory Registry for use by this test
     */
    public abstract PoddFileReferenceProcessorFactoryRegistry getNewPoddFileReferenceProcessorFactoryRegistry();
    
    @Before
    public void setUp() throws Exception
    {
        this.testRegistry = this.getNewPoddFileReferenceProcessorFactoryRegistry();
        Assert.assertNotNull("Null implementation of test Registry", this.testRegistry);
        
        this.testFileReferenceManager = this.getNewPoddFileReferenceManager();
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
        Set<PoddFileReference> extractedFileReferences =
                this.testFileReferenceManager.extractFileReferences(testRepositoryConnection, someContext);
        // FIXME
    }
    
    // TODO add more tests
}
