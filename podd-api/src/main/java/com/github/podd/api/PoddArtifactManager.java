/**
 * 
 */
package com.github.podd.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.file.PoddFileReferenceManager;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PublishArtifactException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
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
     * Exports the given artifact to the given output stream using an RDF format.
     * 
     * @param ontologyId
     *            The {@link InferredOWLOntologyID} of the artifact to export. This must contain
     *            both an ontology IRI and a version IRI.
     * @param outputStream
     *            The {@link OutputStream} to export the RDF statements to.
     * @param format
     *            The {@link RDFFormat} for the exported RDF statements.
     * @param includeInferred
     *            If true, inferred statements will be included in the exported RDF statements,
     *            otherwise if false, only the concrete RDF statements will be exported.
     * @throws OpenRDFException
     *             If there is an error communicating the RDF storage for the artifact.
     * @throws PoddException
     *             If there is an error in the PODD methods.
     * @throws IOException
     *             If there is an error accessing the RDF storage, or an error writing to the output
     *             stream.
     */
    void exportArtifact(InferredOWLOntologyID ontologyId, OutputStream outputStream, RDFFormat format,
            boolean includeInferred) throws OpenRDFException, PoddException, IOException;
    
    /**
     * Returns the {@link InferredOWLOntologyID} for the artifact identified by the given IRI.
     * 
     * If the IRI maps to more than one version of an artifact, the most current version of the
     * artifact is returned.
     * 
     * @param artifactIRI
     *            The IRI of the Artifact to fetch. Can be either the version or the ontology IRI.
     * @return An {@link InferredOWLOntologyID} containing the full details for the artifact.
     * @throws UnmanagedArtifactIRIException
     *             If the artifact is not managed.
     */
    InferredOWLOntologyID getArtifactByIRI(IRI artifactIRI) throws UnmanagedArtifactIRIException;
    
    /**
     * 
     * @return The {@link PoddFileReferenceManager} used to manage file references for artifacts.
     */
    PoddFileReferenceManager getFileReferenceManager();
    
    /**
     * 
     * @return The {@link PoddOWLManager} used to manage OWL validation and inferencing for
     *         artifacts.
     */
    PoddOWLManager getOWLManager();
    
    /**
     * 
     * @return The {@link PoddPurlManager} used to manage PURL creation and validation for URIs in
     *         artifacts.
     */
    PoddPurlManager getPurlManager();
    
    /**
     * 
     * @return The {@link PoddRepositoryManager} used to manage access to the {@link Repository}.
     */
    PoddRepositoryManager getRepositoryManager();
    
    /**
     * 
     * @return The {@link PoddSchemaManager} used to access and verify versions of Schema Ontologies
     *         used in artifacts.
     */
    PoddSchemaManager getSchemaManager();
    
    /**
     * 
     * @return The {@link PoddSesameManager} used to perform operations on a Sesame Repository
     */
    PoddSesameManager getSesameManager();
    
    /**
     * Loads an artifact into the manager.
     * 
     * NOTE: After this method completes the Artifact may no longer be in memory in the
     * {@link PoddOWLManager}, but will be stored in the underlying Sesame {@link Repository}.
     * 
     * @param inputStream
     *            The input stream containing the RDF document for the updated artifact.
     * @param format
     *            The format for the input RDF document.
     * @return An InferredOWLOntologyID object containing the details of the artifact. If the
     *         inferencing is delayed, the object may not contain the inferred ontology IRI.
     * @throws IOException
     * @throws PoddException
     * @throws OpenRDFException
     * @throws OWLException
     */
    InferredOWLOntologyID loadArtifact(InputStream inputStream, RDFFormat format) throws OpenRDFException,
        PoddException, IOException, OWLException;
    
    /**
     * Sets the given OWLOntologyID to be published.
     * 
     * NOTE: After publication PODD Artifacts cannot be modified. New versions must be created as
     * new PODD Artifacts, which may then be published when they are ready.
     * 
     * @param ontologyId
     *            The OWL Ontology ID of the PODD Artifact that needs to be published.
     * @return The full inferred OWL Ontology ID of the published Artifact.
     * @throws PublishArtifactException
     *             If the artifact could not be published for any reason.
     * @throws OpenRDFException
     * @throws UnmanagedArtifactIRIException
     *             If this is not a managed artifact
     */
    InferredOWLOntologyID publishArtifact(OWLOntologyID ontologyId) throws PublishArtifactException, OpenRDFException,
        UnmanagedArtifactIRIException;
    
    void setFileReferenceManager(PoddFileReferenceManager fileManager);
    
    /**
     * Sets the {@link PoddOWLManager} instance to use when loading and dealing with Artifacts in
     * memory. This manager may not be used for some queries where SPARQL queries on the underlying
     * Repository can more efficiently complete the operation.
     * 
     * NOTE: Artifacts are not necessarily cached in memory, so no manual cleanup is needed if
     * calling methods from the PoddArtifactManager interface.
     * 
     * @param owlManager
     *            The manager for interactions with OWLAPI.
     */
    void setOwlManager(PoddOWLManager owlManager);
    
    void setPurlManager(PoddPurlManager purlManager);
    
    void setRepositoryManager(PoddRepositoryManager repositoryManager);
    
    void setSchemaManager(PoddSchemaManager schemaManager);
    
    void setSesameManager(PoddSesameManager sesameManager);
    
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
