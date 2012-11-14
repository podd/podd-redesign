/**
 * 
 */
package com.github.podd.api;

import java.util.List;

import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Manages PODD Artifacts, including loading and retrieving the ontologies representing the
 * artifacts and the association of artifacts with schema ontologies.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddArtifactManager
{
    /**
     * 
     * @param purlGenerator
     *            A PURL Generator that no longer needs to be associated with this
     *            PoddArtifactManager.
     */
    void deregisterPurlGenerator(PoddPurlGenerator purlGenerator);
    
    /**
     * 
     * @return The current List of PoddPurlGenerator's that are registered with this
     *         PoddArtifactManager to provide PURL Generation services for loading and publishing
     *         objects.
     */
    List<PoddPurlGenerator> getPurlGenerators();
    
    /**
     * 
     * NOTE: After publication PODD Artifacts cannot be modified. New versions must be created as
     * new PODD Artifacts, which may then be published when they are ready.
     * 
     * @param ontologyId
     *            The OWL Ontology ID of the PODD Artifact that needs to be published.
     * @return The full inferred OWL Ontology ID of the published Artifact.
     */
    InferredOWLOntologyID publishArtifact(OWLOntologyID ontologyId);
    
    /**
     * 
     * @param purlGenerator
     *            A PURL Generator that needs to be associated with this PoddArtifactManager.
     */
    void registerPurlGenerator(PoddPurlGenerator purlGenerator);
    
    /**
     * Updates the importing of the given schema ontology in the given PODD Artifact.
     * 
     * The Schema Ontology may have been imported as a different version, and these older versions
     * must be identified during this process to import the given version of the schema ontology in
     * place of the old version.
     * 
     * If the Schema Ontology was not previously imported by this PODD Artifact, then an import
     * statement will be added to indicate that the PODD Artifact imports the given version of the
     * Podd Schema Ontology.
     * 
     * @param artifactId
     *            The Ontology ID for the PODD Artifact which needs to have its Schema Ontology
     *            imports modified.
     * @param schemaOntologyId
     *            The Ontology ID for the Schema Ontology which needs to be added or modified in the
     *            imports for the PODD Artifact.
     */
    void updateSchemaImport(OWLOntologyID artifactId, OWLOntologyID schemaOntologyId);
}
