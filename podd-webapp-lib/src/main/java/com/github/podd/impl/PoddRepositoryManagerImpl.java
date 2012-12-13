/**
 * 
 */
package com.github.podd.impl;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.sail.memory.MemoryStore;

import com.github.podd.api.PoddRepositoryManager;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public class PoddRepositoryManagerImpl implements PoddRepositoryManager
{
    
    private Repository repository;
    
    /**
     * Default constructor, which sets up an in-memory MemoryStore repository.
     */
    public PoddRepositoryManagerImpl()
    {
        this.repository = new SailRepository(new MemoryStore());
        try
        {
            this.repository.initialize();
        }
        catch(RepositoryException e)
        {
            throw new RuntimeException("Could not initialise PoddRepositoryManager with an in-memory repository");
        }
    }
    
    /**
     * 
     * @param repository
     *            An initialized implementation of Repository.
     */
    public PoddRepositoryManagerImpl(Repository repository)
    {
        this.repository = repository;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see com.github.podd.api.PoddRepositoryManager#getRepository()
     */
    @Override
    public Repository getRepository() throws OpenRDFException
    {
        return this.repository;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.github.podd.api.PoddRepositoryManager#setRepository(org.openrdf.repository.Repository)
     */
    @Override
    public void setRepository(Repository repository) throws OpenRDFException
    {
        this.repository = repository;
    }
    
}
