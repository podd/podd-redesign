/**
 * 
 */
package com.github.podd.api;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
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
     * @return A link to the initialised repository managed by this manager.
     * @throws OpenRDFException
     *             If there are any errors with the repository at this stage.
     */
    Repository getRepository() throws OpenRDFException;
    
    /**
     * 
     * @return A new, initialized in-memory repository that can be used to store statements
     *         temporarily while validating them before uploading them to a permanent repository.
     * @throws OpenRDFException
     */
    Repository getNewTemporaryRepository() throws OpenRDFException;
    
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
     * Sets the artifact management graph URI for use with this repository manager.
     * 
     * @param artifactManagementGraph
     */
    void setArtifactManagementGraph(URI artifactManagementGraph);
    
    /**
     * Sets the schema management graph URI for use with this repository manager.
     * 
     * @param schemaManagementGraph
     */
    void setSchemaManagementGraph(URI schemaManagementGraph);
    
    /**
     * 
     * @return The schema management graph URI for this repository manager.
     */
    URI getSchemaManagementGraph();
    
    /**
     * 
     * @return The artifact management graph URI for this repository manager.
     */
    URI getArtifactManagementGraph();
}
