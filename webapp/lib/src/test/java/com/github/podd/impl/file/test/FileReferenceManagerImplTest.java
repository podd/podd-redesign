/**
 * 
 */
package com.github.podd.impl.file.test;

import com.github.podd.api.file.DataReferenceManager;
import com.github.podd.api.file.DataReferenceProcessorRegistry;
import com.github.podd.api.file.test.AbstractDataReferenceManagerTest;
import com.github.podd.impl.file.FileReferenceManagerImpl;

/**
 * Concrete test for DataReferenceManager
 * 
 * @author kutila
 */
public class FileReferenceManagerImplTest extends AbstractDataReferenceManagerTest
{
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.file.test.AbstractDataReferenceManagerTest#getNewFileReferenceManager()
     */
    @Override
    public DataReferenceManager getNewDataReferenceManager()
    {
        final FileReferenceManagerImpl fileRefManager = new FileReferenceManagerImpl();
        return fileRefManager;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.file.test.AbstractDataReferenceManagerTest#
     * getNewPoddFileReferenceProcessorFactoryRegistry()
     */
    @Override
    public DataReferenceProcessorRegistry getNewDataReferenceProcessorRegistry()
    {
        final DataReferenceProcessorRegistry registry = new DataReferenceProcessorRegistry();
        
        // this should happen automatically
        // registry.add(new SSHFileReferenceProcessorFactoryImpl());
        
        return registry;
    }
    
}
