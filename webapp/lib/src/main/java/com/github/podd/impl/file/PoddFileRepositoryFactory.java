/**
 * 
 */
package com.github.podd.impl.file;

import org.openrdf.model.Model;
import org.openrdf.model.vocabulary.RDF;

import com.github.podd.api.file.PoddFileRepository;
import com.github.podd.exception.FileRepositoryIncompleteException;
import com.github.podd.utils.PoddRdfConstants;

/**
 * A simple static factory to build {@link PoddFileRepository} instances from a {@link Model}.
 * 
 * @author kutila
 * 
 */
public class PoddFileRepositoryFactory
{
    /**
     * Create a new {@link PoddFileRepository} instance based on the contents of the given
     * {@link Model} and alias.
     * 
     * @param alias
     *            A unique String
     * @param model
     *            Contains the configurations for a FileRepository
     * @return A newly generated File Repository configuration
     * @throws FileRepositoryIncompleteException
     *             If there was insufficient data to create a new File Repository configuration
     */
    public static PoddFileRepository<?> createFileRepository(final String alias, final Model model)
        throws FileRepositoryIncompleteException
    {
        if(alias == null || model == null || model.isEmpty())
        {
            throw new FileRepositoryIncompleteException(model, "Insufficient data to create a FileRepository");
        }
        
        PoddFileRepository<?> fileRepository = null;
        
        if(!model.filter(null, RDF.TYPE, PoddRdfConstants.PODD_SSH_FILE_REPOSITORY).isEmpty())
        {
            fileRepository = new SSHFileRepositoryImpl(model);
        }
        else
        {
            throw new FileRepositoryIncompleteException(model, "No known FileRepository type to create an instance");
        }
        return fileRepository;
    }
}
