/**
 * 
 */
package com.github.podd.api;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * Encapsulates File References that are tracked inside of PODD Artifacts.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface FileReference
{
    /**
     * 
     * @return The {@link OWLOntologyID} for the Artifact that includes this file reference.
     */
    public OWLOntologyID getArtifactID();
    
    /**
     * 
     * @return The {@link IRI} of the object inside of the Artifact that this file reference is
     *         linked to.
     */
    public IRI getObjectIri();
    
    /**
     * 
     * @return The alias for the repository that is managing this file reference.
     */
    public String getRepositoryAlias();
    
    /**
     * 
     * @param artifactUri
     *            The {@link OWLOntologyID} for the Artifact that includes this file reference.
     */
    public void setArtifactID(OWLOntologyID artifactUri);
    
    /**
     * 
     * @param objectUri
     *            The {@link IRI} of the object inside of the Artifact that this file reference is
     *            linked to.
     */
    public void setObjectIri(IRI objectUri);
    
    /**
     * Sets the alias used to name the repository configuration used to access this file reference.
     * 
     * @param repositoryAlias
     *            Alias for the repository that is managing this file reference.
     */
    public void setRepositoryAlias(String repositoryAlias);
}
