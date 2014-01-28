/**
 * PODD is an OWL ontology database used for scientific project management
 * 
 * Copyright (C) 2009-2013 The University Of Queensland
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.podd.api.file;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;

import com.github.podd.api.PoddOWLManager;
import com.github.podd.api.PoddRepositoryManager;
import com.github.podd.api.data.DataReference;
import com.github.podd.exception.DataRepositoryException;
import com.github.podd.exception.DataRepositoryMappingNotFoundException;
import com.github.podd.exception.PoddException;

/**
 * A manager object used to manage configurations for file repositories.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddDataRepositoryManager
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
     * @throws DataRepositoryException
     */
    void addRepositoryMapping(String alias, PoddDataRepository<?> repositoryConfiguration) throws OpenRDFException,
        DataRepositoryException;
    
    void addRepositoryMapping(String alias, PoddDataRepository<?> repositoryConfiguration, boolean overwrite)
        throws OpenRDFException, DataRepositoryException;
    
    void downloadFileReference(DataReference nextFileReference, OutputStream outputStream) throws PoddException,
        IOException;
    
    /**
     * Returns a list of all aliases that are currently mapped.
     * 
     * @return
     * @throws DataRepositoryException
     * @throws OpenRDFException
     */
    List<String> getAllAliases() throws DataRepositoryException, OpenRDFException;
    
    /**
     * Given an alias returns all aliases, including the given one, that map to the same repository
     * configuration.
     * 
     * @param alias
     * @return Returns a list of all aliases that are currently mapped to the same repository
     *         configuration as the given alias.
     * @throws DataRepositoryException
     * @throws OpenRDFException
     */
    List<String> getEquivalentAliases(String alias) throws DataRepositoryException, OpenRDFException;
    
    PoddOWLManager getOWLManager();
    
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
     * @throws DataRepositoryException
     *             If a mapped PoddDataRepository could not be constructed due to a problem with
     *             available data.
     * @throws OpenRDFException
     */
    PoddDataRepository<?> getRepository(String alias) throws OpenRDFException, DataRepositoryException;
    
    /**
     * Returns the aliases that are currently being mapped using the given repository configuration.
     * 
     * @param repositoryConfiguration
     * @return Returns a list containing the aliases that are currently mapped to the given
     *         repository configuration.
     * @throws OpenRDFException
     * @throws DataRepositoryException
     */
    List<String> getRepositoryAliases(PoddDataRepository<?> repositoryConfiguration) throws DataRepositoryException,
        OpenRDFException;
    
    PoddRepositoryManager getRepositoryManager();
    
    /**
     * Checks to see if the File Repository Management Graph contains any data, and if empty loads a
     * default set of repository configurations and aliases from the specified model.
     * 
     * @param defaultAliasesConfiguraton
     *            A model containing the default aliases configuration to be used if necessary.
     * @throws OpenRDFException
     * @throws DataRepositoryException
     * @throws IOException
     * @throws PoddException
     */
    void initialise(Model defaultAliasesConfiguraton) throws OpenRDFException, IOException, PoddException;
    
    /**
     * Removes the mapping for the given alias, returning the {@link PoddDataRepository} object that
     * was previously mapped to using the alias.
     * 
     * @param alias
     *            The alias used to map a repository configuration to
     * @return The file repository configuration that was previously mapped to the given alias.
     * @throws DataRepositoryMappingNotFoundException
     *             If the alias was not found in the current set of mappings.
     * @throws OpenRDFException
     * @throws DataRepositoryException
     */
    PoddDataRepository<?> removeRepositoryMapping(String alias) throws DataRepositoryMappingNotFoundException,
        DataRepositoryException, OpenRDFException;
    
    void setOWLManager(PoddOWLManager owlManager);
    
    void setRepositoryManager(PoddRepositoryManager repositoryManager);
    
    /**
     * Verifies that a given set of {@link DataReference} objects are valid by checking they can be
     * accessed from the remote Data Repository.
     * 
     * @param dataReferenceResults
     *            The set of {@link DataReference}s to be verified
     * @throws OpenRDFException
     * @throws PoddException
     * @throws DataRepositoryMappingNotFoundException
     *             If at least one of the FileReferences fail validation, this Exception is thrown
     *             containing a Map which contains the offending FileReferences and their causes of
     *             failure.
     */
    void verifyDataReferences(Set<DataReference> dataReferenceResults) throws OpenRDFException, PoddException,
        DataRepositoryMappingNotFoundException;
    
}
