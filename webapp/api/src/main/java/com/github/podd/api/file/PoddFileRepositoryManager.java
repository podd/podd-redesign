package com.github.podd.api.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;

import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.exception.FileRepositoryException;
import com.github.podd.exception.FileRepositoryMappingNotFoundException;
import com.github.podd.exception.PoddException;

/**
 * A manager object used to manage configurations for file repositories.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddFileRepositoryManager
{
    /**
     * Adds a mapping between the given String <i>alias</i> and the given repository configuration.
     * 
     * This mapping is necessary to link file references in PODD Artifacts with the correct file
     * repositories.
     * 
     * The alias used here may be arbitrary, but it must be used consistently to refer to a single
     * file repository configuration, which may include authentication and role data that is
     * different to other file repository configurations targeting the same file repository
     * provider.
     * 
     * NOTE: The same repository configuration can be mapped to many aliases, as appropriate to
     * match the file references in PODD Artifacts managed by this system.
     * 
     * @param alias
     *            The string to map the given repository configuration to.
     * @param repositoryConfiguration
     *            The repository configuration object to add a mapping for.
     * @throws OpenRDFException
     * @throws FileRepositoryException
     */
    void addRepositoryMapping(String alias, PoddFileRepository<?> repositoryConfiguration) throws OpenRDFException,
        FileRepositoryException;
    
    void addRepositoryMapping(String alias, PoddFileRepository<?> repositoryConfiguration, boolean overwrite)
        throws OpenRDFException, FileRepositoryException;
    
    /**
     * Returns the repository configuration that is currently mapped using the given alias.
     * 
     * @param alias
     *            The alias for a file repository. This is a string that may be different to the IRI
     *            used in the repository configuration. It must be able to be overridden in future
     *            to provide access to file repositories that are migrated between locations or
     *            authentication credentials are modified.
     * @return The file repository configuration targeted by the given alias or NULL if the alias is
     *         not mapped to a file repository.
     * @throws FileRepositoryException
     *             If a mapped PoddFileRepository could not be constructed due to a problem with
     *             available data.
     * @throws OpenRDFException
     */
    PoddFileRepository<?> getRepository(String alias) throws OpenRDFException, FileRepositoryException;
    
    /**
     * Returns the aliases that are currently being mapped using the given repository configuration.
     * 
     * @param repositoryConfiguration
     * @return Returns a list containing the aliases that are currently mapped to the given
     *         repository configuration.
     * @throws OpenRDFException
     * @throws FileRepositoryException
     */
    List<String> getRepositoryAliases(PoddFileRepository<?> repositoryConfiguration) throws FileRepositoryException,
        OpenRDFException;

    /**
     * Given an alias returns all aliases, including the given one, that map to the same repository
     * configuration.
     * 
     * @param alias
     * @return Returns a list of all aliases that are currently mapped to the same repository
     *         configuration as the given alias.
     * @throws FileRepositoryException
     * @throws OpenRDFException
     */
    List<String> getEquivalentAliases(String alias) throws FileRepositoryException, OpenRDFException;

    /**
     * Removes the mapping for the given alias, returning the {@link PoddFileRepository} object that
     * was previously mapped to using the alias.
     * 
     * @param alias
     *            The alias used to map a repository configuration to
     * @return The file repository configuration that was previously mapped to the given alias.
     * @throws FileRepositoryMappingNotFoundException
     *             If the alias was not found in the current set of mappings.
     * @throws OpenRDFException
     * @throws FileRepositoryException
     */
    PoddFileRepository<?> removeRepositoryMapping(String alias) throws FileRepositoryMappingNotFoundException,
        FileRepositoryException, OpenRDFException;
    
    /**
     * Verifies that a given set of {@link FileReference} objects are valid by checking they can be 
     * accessed from the remote File Repository.
     * 
     * @param fileReferenceResults
     *            The set of FileReferences to be verified
     * @throws OpenRDFException
     * @throws PoddException
     * @throws FileRepositoryMappingNotFoundException
     *             If at least one of the FileReferences fail validation, this Exception is thrown
     *             containing a Map which contains the offending FileReferences and their causes of
     *             failure.
     */
    void verifyFileReferences(Set<FileReference> fileReferenceResults) throws OpenRDFException, PoddException,
        FileRepositoryMappingNotFoundException;
    
    void downloadFileReference(FileReference nextFileReference, OutputStream outputStream) throws PoddException,
        IOException;
    
    void setRepositoryManager(PoddRepositoryManager repositoryManager);
    
    PoddRepositoryManager getRepositoryManager();

}
