package com.github.podd.api.file;

import java.util.List;

import com.github.podd.exception.FileRepositoryMappingNotFoundException;

/**
 * A manager object used to manage configurations for file repositories.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddFileRepositoryManager
{
    /**
     * Adds a mapping between the given IRI and the given repository configuration.
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
     */
    void addRepositoryMapping(String alias, PoddFileRepository repositoryConfiguration);
    
    /**
     * Returns the repository configuration that is currently mapped using the given alias.
     * 
     * @param alias
     *            The alias for a file repository. This is a string that may be different to the IRI
     *            used in the repository configuration. It must be able to be overridden in future
     *            to provide access to file repositories that are migrated between locations or
     *            authentication credentials are modified.
     * @return The file repository configuration targeted by the given alias.
     * @throws FileRepositoryMappingNotFoundException
     *             If the alias was not found in the current set of mappings.
     */
    PoddFileRepository getRepository(String alias) throws FileRepositoryMappingNotFoundException;
    
    /**
     * Returns the aliases that are currently being mapped using the given repository configuration.
     * 
     * @param repositoryConfiguration
     * @return Returns a list containing the aliases that are currently mapped to the given
     *         repository configuration.
     */
    List<String> getRepositoryAlias(PoddFileRepository repositoryConfiguration);
    
    /**
     * Removes the mapping for the given alias, returning the {@link PoddFileRepository} object that
     * was previously mapped to using the alias.
     * 
     * @param alias
     *            The alias used to map a repository configuration to
     * @return The file repository configuration that was previously mapped to the given alias.
     * @throws FileRepositoryMappingNotFoundException
     *             If the alias was not found in the current set of mappings.
     */
    PoddFileRepository removeRepositoryMapping(String alias) throws FileRepositoryMappingNotFoundException;
}
