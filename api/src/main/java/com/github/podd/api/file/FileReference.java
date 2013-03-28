/**
 * 
 */
package com.github.podd.api.file;

import org.semanticweb.owlapi.model.IRI;

import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Encapsulates File References that are tracked inside of PODD Artifacts.
 * 
 * More specific interfaces should be used to represent particular types of file references. For
 * example, SSH file reference objects may be distinctly different to DOI file reference objects.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * 
 */
public interface FileReference
{
    /**
     * 
     * @return The {@link InferredOWLOntologyID} for the Artifact that includes this file reference.
     */
    InferredOWLOntologyID getArtifactID();
    
    /**
     * Returns the label that is assigned to this file reference.
     * 
     * This property is mapped to rdfs:label in the corresponding ontology.
     * 
     * @return A human readable label for this file reference.
     */
    String getLabel();
    
    /**
     * 
     * @return The {@link IRI} of this file reference.
     */
    IRI getObjectIri();
    
    /**
     * 
     * @return The {@link IRI} of the object inside of the Artifact that this file reference is
     *         linked to.
     */
    IRI getParentIri();

    /**
     * 
     * @return The alias for the repository that is managing this file reference.
     */
    String getRepositoryAlias();
    
    /**
     * 
     * @param artifactId
     *            The {@link InferredOWLOntologyID} for the Artifact that includes this file reference.
     */
    void setArtifactID(InferredOWLOntologyID artifactId);
    
    /**
     * Sets a human readable label for this file reference.
     * 
     * This property must be mapped to the rdfs:label annotation property.
     * 
     * @param label
     *            A human readable label for this file reference.
     */
    void setLabel(String label);
    
    /**
     * 
     * @param objectIri
     *            The {@link IRI} of this file reference.
     */
    void setObjectIri(IRI objectIri);
    
    /**
     * 
     * @param parentIri
     *            The {@link IRI} of the object inside of the Artifact that this file reference is
     *            linked to.
     */
    void setParentIri(IRI parentIri);

    /**
     * Sets the alias used to name the repository configuration used to access this file reference.
     * 
     * @param repositoryAlias
     *            Alias for the repository that is managing this file reference.
     */
    void setRepositoryAlias(String repositoryAlias);
}
