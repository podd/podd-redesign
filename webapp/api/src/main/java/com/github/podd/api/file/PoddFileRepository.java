/**
 * 
 */
package com.github.podd.api.file;

import java.util.Set;

import org.openrdf.model.URI;

/**
 * This interface represents the basic type for all file repositories. PODD uses File Repositories
 * to represent different ways of accessing binary data that is not stored inside of PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddFileRepository<T extends PoddFileReference>
{
    String getAlias();
    
    /**
     * 
     * @return The set of RDF Types for {@link PoddFileReference}s that can be stored by this
     *         repository. All of the non-OWL-built-in types should be in this set.
     */
    Set<URI> getTypes();
    
    // throw exception if this file reference cannot be handled by this repository
    boolean validate(T reference);
    
    /**
     * Checks that the alias exists and type of Repository
     * 
     * @param reference
     * @return
     */
    boolean canHandle(T reference);
    
}
