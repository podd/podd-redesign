/**
 * 
 */
package com.github.podd.api.file;

import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

import com.github.podd.exception.DataRepositoryException;

/**
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddDataRepositoryFactory
{
    /**
     * 
     * @param types
     *            The type URIs for the given repository that needs to be created.
     * @return True if this factory can create a repository to access all of the given type URIs.
     */
    boolean canCreate(Set<URI> types);
    
    /**
     * 
     * @param statements
     *            The RDF statements defining the repository configuration.
     * @return A new instance of the repository.
     * @throws DataRepositoryException
     *             If there was an error creating the repository.
     */
    PoddDataRepository<?> createDataRepository(Model statements) throws DataRepositoryException;
    
    /**
     * @return A Unique identifying string for this instance of this factory.
     */
    String getKey();
}
