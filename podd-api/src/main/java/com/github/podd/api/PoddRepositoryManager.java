/**
 * 
 */
package com.github.podd.api;

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
     * @return A link to the initialised repository managed by this manager.
     * @throws OpenRDFException
     *             If there are any errors with the repository at this stage.
     */
    Repository getRepository() throws OpenRDFException;
    
    /**
     * 
     * @return A new in-memory repository that can be used to store statements temporarily while
     *         validating them before uploading them to a permanent repository.
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
    
    void setArtifactManagementGraph(URI artifactManagementGraph);
    
    void setSchemaManagementGraph(URI schemaManagementGraph);
    
    void updateCurrentManagedSchemaOntologyVersion(OWLOntologyID nextOntologyID, OWLOntologyID nextInferredOntologyID,
            boolean updateCurrent) throws RepositoryException;
    
    void updateManagedPoddArtifactVersion(OWLOntologyID nextOntologyID, OWLOntologyID nextInferredOntologyID,
            boolean updateCurrent) throws RepositoryException;
}
