/**
 * 
 */
package com.github.podd.api.file;

import java.io.IOException;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;

import com.github.podd.exception.FileReferenceNotSupportedException;

/**
 * This interface represents the basic type for all file repositories. PODD uses File Repositories
 * to represent different ways of accessing binary data that is not stored inside of PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddFileRepository<T extends FileReference>
{
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
    
    String getAlias();
    
    /**
     * Retrieve a Model representation of all this FileRepository. This should contain sufficient
     * information to reconstruct this FileRepository object.
     * 
     * @return A Model representation containing all configurations of this FileRepository.
     */
    Model getAsModel();
    
    /**
     * 
     * @return The set of RDF Types for {@link FileReference}s that can be stored by this
     *         repository. All of the non-OWL-built-in types should be in this set.
     */
    Set<URI> getTypes();
    
    /**
     * Validates the given FileReference instance.
     * 
     * @param reference
     *            The FileReference to be validated
     * @return True if the validation was successful, false otherwise
     * @throws FileReferenceNotSupportedException
     * @throws IOException
     */
    boolean validate(T reference) throws FileReferenceNotSupportedException, IOException;
    
}
