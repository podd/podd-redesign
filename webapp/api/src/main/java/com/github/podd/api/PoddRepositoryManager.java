/**
 * 
 */
package com.github.podd.api;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;

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
     * 
     * @return A new, initialized in-memory repository that can be used to store statements
     *         temporarily while validating them before uploading them to a permanent repository.
     * @throws OpenRDFException
     */
    Repository getNewTemporaryRepository() throws OpenRDFException;
    
    /**
     * 
     * @return A link to the initialised repository managed by this manager.
     * @throws OpenRDFException
     *             If there are any errors with the repository at this stage.
     */
    Repository getRepository() throws OpenRDFException;
    
    /**
     * 
     * @return The schema management graph URI for this repository manager.
     */
    URI getSchemaManagementGraph();
    
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
    void setRepository(Repository repository) throws OpenRDFException;
    
    /**
     * Sets the schema management graph URI for use with this repository manager.
     * 
     * @param schemaManagementGraph
     */
    void setSchemaManagementGraph(URI schemaManagementGraph);
    
}
