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
/**
 * 
 */
package com.github.podd.api;

import java.io.IOException;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * Interface to manage the Sesame Repository used by PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface PoddRepositoryManager
{
    /**
     * 
     * @return The artifact management graph URI for this repository manager.
     */
    URI getArtifactManagementGraph();
    
    /**
     * 
     * @return The file repository management graph URI for this repository manager.
     */
    URI getFileRepositoryManagementGraph();
    
    /**
     * Gets a reference to the
     * 
     * @return A link to the initialised management repository managed by this manager.
     * @throws OpenRDFException
     *             If there are any errors with the repository at this stage.
     */
    Repository getManagementRepository() throws OpenRDFException;
    
    /**
     * Get a new temporary repository.
     * 
     * @return A new, initialized in-memory repository that can be used to store statements
     *         temporarily while validating them before uploading them to a permanent repository.
     * @throws OpenRDFException
     */
    Repository getNewTemporaryRepository() throws OpenRDFException;
    
    /**
     * 
     * @param schemaOntologies
     *            A list of schema ontologies that must be supported by the permanent repository.
     * 
     * @return A link to the initialised repository managed by this manager.
     * @throws OpenRDFException
     *             If there are any errors with the repository at this stage.
     * @throws IOException
     *             If there are errors finding the repository.
     */
    Repository getPermanentRepository(Set<? extends OWLOntologyID> schemaOntologies) throws OpenRDFException,
        IOException;
    
    /**
     * Gets a federated repository over the permanent repository for the given schema ontologies,
     * and the management repository.
     * 
     * This repository is read-only to prevent changes across the two repositories.
     * 
     * NOTE: This repository may be much slower than a typical repository.
     * 
     * @param schemaOntologies
     * @return
     * @throws OpenRDFException
     * @throws IOException 
     */
    Repository getReadOnlyFederatedRepository(Set<? extends OWLOntologyID> schemaOntologies) throws OpenRDFException, IOException;
    
    /**
     * 
     * @return The schema management graph URI for this repository manager.
     */
    URI getSchemaManagementGraph();
    
    /**
     * Identfies the given repository as being a suitable target for artifacts importing the given
     * schema ontologies.
     * 
     * @param schemaOntologies
     *            A set of schema ontologies that match the given repository.
     * @param repository
     *            The repository target.
     */
    void mapPermanentRepository(Set<? extends OWLOntologyID> schemaOntologies, Repository repository)
        throws OpenRDFException;
    
    /**
     * Verifies that the context is not null, not the default graph (sesame:nil), and is not one of
     * the management graphs.
     * 
     * @param contexts
     *            The contexts to check.
     * @return True if the contexts are not on the banned or protected list.
     */
    boolean safeContexts(URI... contexts);
    
    /**
     * Sets the artifact management graph URI for use with this repository manager.
     * 
     * @param artifactManagementGraph
     */
    void setArtifactManagementGraph(URI artifactManagementGraph);
    
    /**
     * Sets the file repository management graph URI for use with this repository manager.
     * 
     * @param dataRepositoryManagementGraph
     */
    void setFileRepositoryManagementGraph(URI dataRepositoryManagementGraph);
    
    /**
     * Sets the repository for this repository manager.
     * 
     * @param repository
     *            The new repository to be managed by this repository manager.
     * @throws OpenRDFException
     *             If there are any errors with the repository at this stage.
     */
    void setManagementRepository(Repository repository) throws OpenRDFException;
    
    /**
     * Sets the schema management graph URI for use with this repository manager.
     * 
     * @param schemaManagementGraph
     */
    void setSchemaManagementGraph(URI schemaManagementGraph);
    
    /**
     * Shutdown the repository manager, including any active repositories.
     * 
     * @throws RepositoryException
     *             If there were issues shutting down the active repositories.
     */
    void shutDown() throws RepositoryException;
    
}
