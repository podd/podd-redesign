/**
 * 
 */
package com.github.podd.api;

import java.io.InputStream;

import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Manages PODD Artifacts, including loading and retrieving the ontologies representing the
 * artifacts and the association of artifacts with schema ontologies.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddArtifactManager
{
    PoddFileReferenceManager getFileReferenceManager();
    
    /**
     * Loads an artifact into the manager.
     * 
     * @param inputStream
     *            The input stream containing the RDF document for the updated artifact.
     * @param format
     *            The format for the input RDF document.
     * @return An InferredOWLOntologyID object containing the details of the artifact. If the
     *         inferencing is delayed, the object may not contain the inferred ontology IRI.
     */
    InferredOWLOntologyID loadArtifact(InputStream inputStream, RDFFormat format);
    
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
    
    void setFileReferenceManager(PoddFileReferenceManager fileManager);
    
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
