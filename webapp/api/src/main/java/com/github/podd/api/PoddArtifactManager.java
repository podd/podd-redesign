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
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.restlet.resource.ResourceException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.api.file.DataReference;
import com.github.podd.api.file.DataReferenceManager;
import com.github.podd.api.file.PoddDataRepositoryManager;
import com.github.podd.api.purl.PoddPurlManager;
import com.github.podd.exception.PoddException;
import com.github.podd.exception.PublishArtifactException;
import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedArtifactVersionException;
import com.github.podd.exception.UnmanagedSchemaException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;

/**
 * Manages PODD Artifacts, including loading and retrieving the ontologies representing the
 * artifacts and the association of artifacts with schema ontologies.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddArtifactManager
{
    InferredOWLOntologyID attachFileReference(InferredOWLOntologyID artifactId, URI objectUri,
            DataReference dataReference) throws OpenRDFException, PoddException;
    
    InferredOWLOntologyID attachFileReferences(URI artifactUri, URI versionUri, InputStream inputStream,
            RDFFormat format, DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws OpenRDFException,
        IOException, OWLException, PoddException;
    
    /**
     * Deletes the given artifact if and only if it is available and it is not currently published.
     * <p>
     * If the given artifactId contains a version IRI, then the specific version only must be
     * deleted, as long as it is not currently published.
     * <p>
     * If the artifactId contains a version IRI which is the current version, then another available
     * version may be substituted.
     * <p>
     * To ensure that all versions are deleted the user must ensure that the artifact is not
     * currently published, and that there is no version IRI attached to the given artifactId.
     * 
     * @param artifactId
     *            The {@link OWLOntologyID} containing the details of the artifact to delete.
     * @throws PoddException
     *             If an error occurred while deleting the artifact.
     */
    boolean deleteArtifact(InferredOWLOntologyID artifactId) throws PoddException;
    
    /**
     * Deletes the specified PODD object within an artifact. The artifact containing the 
     * object should not be currently published.
     * 
     * @param artifactUri
     * @param versionUri
     * @param objectUri
     * @param cascade
     * @return Updated InferredOWLOntologyID of artifact after deletion
     * @throws PoddException
     * @throws OWLException 
     * @throws IOException 
     * @throws RepositoryException 
     */
    InferredOWLOntologyID deleteObject(String artifactUri, String versionUri, String objectUri, boolean cascade)
            throws PoddException, OpenRDFException, IOException, OWLException;

    /**
     * Exports the given artifact to a @{link Model}.
     * 
     * @param ontologyId
     *            The {@link InferredOWLOntologyID} of the artifact to export. This must contain
     *            both an ontology IRI and a version IRI.
     * @param includeInferred
     *            If true, inferred statements will be included in the exported RDF statements,
     *            otherwise if false, only the concrete RDF statements will be exported.
     * @return A Model containing the artifact's statements.
     * @throws OpenRDFException
     *             If there is an error communicating the RDF storage for the artifact.
     * @throws PoddException
     *             If there is an error in the PODD methods.
     * @throws IOException
     *             If there is an error accessing the RDF storage, or an error writing to the output
     *             stream.
     */
    Model exportArtifact(InferredOWLOntologyID ontologyId, boolean includeInferred) throws OpenRDFException,
    PoddException, IOException;
    
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
     * Exports metadata about the given object Type to the given output stream using an RDF Format.
     * This method is only to be used to obtain metadata for display purposes.
     * 
     * @param objectType
     * @param outputStream
     * @param format
     * @param includeDoNotDisplayProperties
     * @param containsPropertyPolicy
     *            Indicates types of properties to be included
     * @param artifactID
     *            If present, the artifact ID is used to select the schema ontologies from which
     *            necessary metadata is to be extracted
     * @throws OpenRDFException
     * @throws PoddException
     * @throws IOException
     */
    void exportObjectMetadata(URI objectType, OutputStream outputStream, RDFFormat format,
            boolean includeDoNotDisplayProperties, MetadataPolicy containsPropertyPolicy,
            final InferredOWLOntologyID artifactID) throws OpenRDFException, PoddException, IOException;
    
    /**
     * This method takes in a {@link Model} where the statements have missing data and attempts to
     * fill this data using information from the graphs of the given ontology's import closure. <br>
     * 
     * Missing data is identified by special placeholder nodes. The placeholder for a missing object
     * value is the String Literal "?blank".
     * 
     * <br>
     * <br>
     * 
     * TODO: The current implementation of this method can only handle missing rdfs:labels. <br>
     * 
     * @param ontologyID
     *            The ontology whose import closure is used to look for missing information.
     * @param inputModel
     *            Contains the statements with missing data.
     * @return The statements with missing data completed. Any statements for which data could not
     *         be found are omitted from the results.
     * @throws OpenRDFException
     */
    Model fillMissingData(InferredOWLOntologyID ontologyID, Model inputModel) throws OpenRDFException;
    
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
    InferredOWLOntologyID getArtifact(IRI artifactIRI) throws UnmanagedArtifactIRIException;
    
    /**
     * Returns the {@link InferredOWLOntologyID} for the artifact identified by the given IRI and
     * version IRI.
     * <p>
     * If versionIRI is null, or the exact version is not available, then null is returned. In these
     * cases, {@link #getArtifact(IRI)} can be used as a backup to attempt to get the most current
     * version.
     * 
     * @param artifactIRI
     *            The IRI of the Artifact to fetch. Can be either the version or the ontology IRI.
     * @return An {@link InferredOWLOntologyID} containing the full details for the artifact or null
     *         if it was not found.
     * @throws UnmanagedArtifactIRIException
     *             If the artifact is not managed.
     */
    InferredOWLOntologyID getArtifact(IRI artifactIRI, IRI versionIRI) throws UnmanagedArtifactIRIException;

    
    /**
     * Returns a {@link Set} containing the Object URIs of the given object's direct children.
     * Direct children are objects that are linked from the given object by a property which is a
     * sub-property of <code>PODDBase:contains</code>. An empty Set is returned if the given object
     * does not have any children.
     * 
     * @param ontologyID
     * @param objectUri
     *            The object whose children are sought.
     * @return A {@link Set} containing the Object URIs of the given object's children
     * @throws OpenRDFException
     */
    Set<URI> getChildObjects(InferredOWLOntologyID ontologyID, URI objectUri) throws OpenRDFException;

    /**
     * 
     * @return The {@link DataReferenceManager} used to create and fetch file references from
     *         artifacts.
     */
    DataReferenceManager getFileReferenceManager();
    
    Set<DataReference> getFileReferences(InferredOWLOntologyID artifactId);
    
    Set<DataReference> getFileReferences(InferredOWLOntologyID artifactId, String alias);
    
    Set<DataReference> getFileReferences(InferredOWLOntologyID artifactId, URI objectUri);
    
    /**
     * 
     * @return The {@link PoddDataRepositoryManager} used to manage external file repository
     *         configurations.
     */
    PoddDataRepositoryManager getFileRepositoryManager();
    
    /**
     * Retrieves a {@link Model} containing all data required for displaying the details of the
     * object in HTML+RDFa.
     * 
     * The returned graph has the following structure.
     * 
     * poddObject :propertyUri :value
     * 
     * propertyUri RDFS:Label "property label"
     * 
     * value RDFS:Label "value label"
     * 
     * @param objectUri
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    Model getObjectDetailsForDisplay(final InferredOWLOntologyID artifactID, final URI objectUri)
        throws OpenRDFException;
    
    /**
     * 
     * @param ontologyID
     *            The given object URI is to be found in this ontology or its imported ontologies.
     * @param objectUri
     *            URI of the object whose label is required
     * @return A {@link Model} containing a single statement which specifies the object's label.
     * @throws OpenRDFException
     */
    PoddObjectLabel getObjectLabel(InferredOWLOntologyID ontologyID, URI objectUri) throws OpenRDFException;
    
    /**
     * Retrieves a list of {@link PoddObjectLabel}s for the most-specific types to which the given
     * object URI belongs.
     * 
     * @param artifactId
     *            The artifact where the given object URI is found.
     * @param objectUri
     *            The object whose types are needed.
     * @return
     * @throws OpenRDFException
     */
    List<PoddObjectLabel> getObjectTypes(InferredOWLOntologyID artifactId, URI objectUri) throws OpenRDFException;
    
    /**
     * Retrieve a list of <b>asserted</b> properties about the given object. The list is ordered
     * based on property weights and secondarily based on property labels.
     * 
     * Properties RDF:Type, RDFS:Comment and RDFS:Label as well as properties whose values are
     * generic OWL concepts (i.e. OWL:Thing, OWL:Individial, OWL:NamedIndividual, OWL:Class) are not
     * included in the results.
     * 
     * Properties with an annotation poddBase:doNotDisplay are also not included in the results.
     * 
     * @param ontologyID
     *            The artifact to which this object belongs
     * @param objectUri
     *            The object whose properties are sought
     * @param excludeContainsProperties
     *            Whether to exclude sub-properties of "poddBase:contains" property
     * @return A List containing URIs of sorted properties about the object
     * 
     * @throws OpenRDFException
     */
    List<URI> getOrderedProperties(InferredOWLOntologyID ontologyID, URI objectUri, boolean excludeContainsProperties)
        throws OpenRDFException;
    
    /**
     * 
     * @return The {@link PoddOWLManager} used to manage OWL validation and inferencing for
     *         artifacts.
     */
    PoddOWLManager getOWLManager();
    
    /**
     * This method returns a {@link Model} containing a single statement which links the given
     * object with its parent object. A <i>parent</i> is connected to the given object by a property
     * which is a sub-property of <code>PODDBase:contains</code>. If the object URI is null or does
     * not have a parent (i.e. it is a <code>PoddTopObject</code>), an empty Model is returned.
     * 
     * @param ontologyID
     *            The ontology to which this object belongs.
     * @param objectUri
     *            The Object whose parent is sought
     * @return A {@link Model} containing a single statement which links the parent with the given
     *         object
     * @throws OpenRDFException
     */
    Model getParentDetails(InferredOWLOntologyID ontologyID, URI objectUri) throws OpenRDFException;
    
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
     * A list of labels for the top objects in the given artifacts.
     * 
     * @param artifacts
     *            A sorted list of artifact identifiers that require labels.
     * @return A list of labels in the same order as the artifacts.
     * @throws OpenRDFException
     */
    List<PoddObjectLabel> getTopObjectLabels(List<InferredOWLOntologyID> artifacts) throws OpenRDFException;
    
    /**
     * Checks whether a given Ontology is Published.
     * 
     * @param ontologyId
     * @return
     * @throws OpenRDFException
     */
    boolean isPublished(InferredOWLOntologyID ontologyId) throws OpenRDFException;
    
    /**
     * 
     * @return The list of artifacts that have been published.
     * @throws OpenRDFException
     */
    List<InferredOWLOntologyID> listPublishedArtifacts() throws OpenRDFException;
    
    /**
     * 
     * @return The list of artifacts that have not been published.
     * @throws OpenRDFException
     */
    List<InferredOWLOntologyID> listUnpublishedArtifacts() throws OpenRDFException;
    
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
    InferredOWLOntologyID publishArtifact(InferredOWLOntologyID ontologyId) throws PublishArtifactException,
        OpenRDFException, UnmanagedArtifactIRIException;
    
    /**
     * Carries out a case-insensitive search for objects whose labels match a given term. The search
     * consists of the given ontology and its imported ontologies. An optional array of URIs can be
     * used to limit the RDF types of objects to match. <br>
     * 
     * @param ontologyID
     *            The ontology and its imported ontologies make up the search space
     * @param searchTerm
     *            A String term which is searched for in the RDF:Labels
     * @param searchTypes
     *            The types (i.e. RDF:Type) of results to match with the search term
     * @return A {@link Model} containing the URI and Label of each matching object.
     * @throws OpenRDFException
     * @throws ResourceException
     */
    Model searchForOntologyLabels(InferredOWLOntologyID ontologyID, String searchTerm, URI[] searchTypes)
        throws OpenRDFException, ResourceException;
    
    /**
     * Sets the {@link DataReferenceManager} to use for verifying file references for PODD
     * artifacts.
     * 
     * @param fileManager
     *            The manager to use for verifying file references for PODD artifacts.
     */
    void setDataReferenceManager(DataReferenceManager fileManager);
    
    /**
     * Sets the {@link PoddDataRepositoryManager} to use for managing file repository
     * configurations.
     * 
     * @param dataRepositoryManager
     *            The manager to use for managing file repository configurations.
     */
    void setDataRepositoryManager(PoddDataRepositoryManager dataRepositoryManager);
    
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
    
    /**
     * Sets the {@link PoddPurlManager} instance to use when processing temporary references into
     * PURLs.
     * 
     * @param purlManager
     *            The manager to use for processing PURLs.
     */
    void setPurlManager(PoddPurlManager purlManager);
    
    /**
     * Sets the {@link PoddRepositoryManager} to use for managing OpenRDF Sesame Repositories used
     * to access and update data for PODD.
     * 
     * @param repositoryManager
     *            The manager to use for managing repositories.
     */
    void setRepositoryManager(PoddRepositoryManager repositoryManager);
    
    /**
     * Sets the {@link PoddSchemaManager} to use for managing the schemas used to validate artifacts
     * for PODD.
     * 
     * @param schemaManager
     *            The manager to use for managing schemas.
     */
    void setSchemaManager(PoddSchemaManager schemaManager);
    
    /**
     * Sets the {@link PoddSesameManager} to use for managing queries to repositories.
     * 
     * @param sesameManager
     *            The manager to use for managing queries to repositories.
     */
    void setSesameManager(PoddSesameManager sesameManager);
    
    /**
     * Updates a managed artifact based on incoming modified RDF statements.
     * 
     * It is sufficient for the input stream to contain only the affected statements. If the update
     * is only addition of new statements, <i>isReplace</i> option can be set to false. If the
     * update involves modifying existing statements (e.g. moving an object or deleting a link
     * between objects) <i>isReplace</i> should be set to true, and all statements making up the
     * modified objects should be included.
     * 
     * @param artifactUri
     *            The URI of the artifact to be updated. This should be an already managed artifact.
     * @param versionUri
     *            The Version URI of the artifact to be updated.
     * @param objectUris
     *            A collection of object URIs to be merged in. If updatePolicy is
     *            {@link UpdatePolicy#REPLACE_EXISTING}, only these object URIs will be updated.
     *            This allows for the new object to be linked to its parent within this update
     *            operation.
     * @param inputStream
     *            The RDF statements that need to be updated. It is not necessary to send the
     *            complete artifact here, only sending the affected statements is sufficient.
     * @param format
     *            Format of the incoming RDF data content
     * @param updatePolicy
     *            Indicates whether the incoming statements should replace existing statements about
     *            the same subjects or whether they should be merged with the existing ones.
     * @param danglingObjectPolicy
     *            The policy for handling any internal PODD objects that become disconnected from
     *            the Top Object as a result of the update. FORCE_CLEAN policy silently deletes any
     *            while REPORT policy throws a DisconnectedObjectException to inform of any
     *            disconnected objects.
     * @param fileReferencePolicy
     *            Indicates whether any File References found should be verified by accessing them
     *            from their source.
     * @return A Model containing the updated artifact ID and PURL mappings for any temporary object
     *         URIs that were passed in.
     * @throws OpenRDFException
     * @throws PoddException
     * @throws IOException
     * @throws OWLException
     */
    Model updateArtifact(URI artifactUri, URI versionUri, Collection<URI> objectUris, InputStream inputStream,
            RDFFormat format, UpdatePolicy updatePolicy, DanglingObjectPolicy danglingObjectAction,
            DataReferenceVerificationPolicy fileReferenceAction) throws OpenRDFException, IOException, OWLException,
        PoddException;
    
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
     * @param oldSchemaOntologyIds
     *            The Ontology IDs for the Schema Ontologies which need to be removed in the imports
     *            for the PODD Artifact.
     * @param newSchemaOntologyIds
     *            The Ontology IDs for the Schema Ontologies which need to be added or modified in
     *            the imports for the PODD Artifact.
     * @return The updated Ontology ID for the PODD Artifact after the schemas were updated.
     * @throws UnmanagedSchemaException
     *             If one of the new schema ontologies are not currently managed by the system.
     * @throws UnmanagedArtifactIRIException
     *             If the given artifact IRI is not currently managed by the system.
     * @throws UnmanagedArtifactVersionException
     *             If the given artifact version is not currently managed by the system.
     * @throws IOException
     *             If there are Input/Output exceptions while updating the schema imports.
     * @throws PoddException
     *             If there are PODD exceptions.
     * @throws OpenRDFException
     *             If there are OpenRDF Sesame exceptions while updating the schema imports.
     * @throws OWLException
     *             If there are errors processing the OWL content during or after the schema imports
     *             updates.
     */
    InferredOWLOntologyID updateSchemaImports(InferredOWLOntologyID artifactId,
            Set<OWLOntologyID> oldSchemaOntologyIds, Set<OWLOntologyID> newSchemaOntologyIds)
        throws UnmanagedSchemaException, OpenRDFException, PoddException, IOException, OWLException;

}
