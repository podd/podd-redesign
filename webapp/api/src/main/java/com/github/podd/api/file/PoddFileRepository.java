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
public interface PoddFileRepository<T extends FileReference>
{
    String getAlias();
    
    /**
     * 
     * @return The set of RDF Types for {@link FileReference}s that can be stored by this
     *         repository. All of the non-OWL-built-in types should be in this set.
     */
    Set<URI> getTypes();
    
    // throw exception if this file reference cannot be handled by this repository
    boolean validate(T reference);
    
    /**
     * Checks whether this File Repository instance is capable of "handling" (i.e. validating) the
     * given File Reference.
     * 
     * This decision is made by checking that the aliases and types match.
     * 
     * @param reference
     * @return
     */
    boolean canHandle(T reference);
    
}
