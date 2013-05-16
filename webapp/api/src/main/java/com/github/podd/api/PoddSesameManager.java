/**
 * 
 */
package com.github.podd.api;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

import com.github.podd.exception.UnmanagedArtifactIRIException;
import com.github.podd.exception.UnmanagedSchemaIRIException;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddObjectLabel;
import com.github.podd.utils.PoddRdfConstants;

/**
 * Manages interactions with Sesame Repositories for PODD.
 * 
 * @author kutila
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddSesameManager
{
    
    /**
     * Deletes the given ontologies, including removing and rearranging their links in the ontology
     * management graph as necessary.
     * 
     * @param requestedArtifactIds
     *            A collection of InferredOWLOntologyID objects containing the ontologies to be
     *            deleted, including the inferred ontology IRIs.
     * @param repositoryConnection
     *            The connection to the repository to use.
     * @param ontologyManagementGraph
     *            The URI of the context in the repository containing the management information for
     *            the ontologies.
     * @throws OpenRDFException
     */
    void deleteOntologies(Collection<InferredOWLOntologyID> requestedArtifactIds,
            RepositoryConnection repositoryConnection, URI ontologyManagementGraph) throws OpenRDFException;
    
    /**
     * Get all versions of the ontology with the given IRI that are managed in the given context in
     * the given repository connection.
     * <p>
     * If there are multiple versions available, then the most current version must be first in the
     * list.
     * 
     * @param ontologyIRI
     *            The Ontology IRI identifying the ontology for which versions must be accessed.
     * @param connection
     *            The repository connection to use when querying for ontology versions.
     * @param ontologyManagementGraph
     *            The URI identifying the context in which the ontologies are managed.
     * @throws OpenRDFException
     */
    List<InferredOWLOntologyID> getAllOntologyVersions(IRI ontologyIRI, RepositoryConnection connection,
            URI ontologyManagementGraph) throws OpenRDFException;
    
    /**
     * Gets all schema ontology versions currently configured in this PODD server.
     * 
     * @param repositoryConnection
     * @param schemaManagementGraph
     * @return
     * @throws OpenRDFException
     * 
     * @since 14/05/2013
     */
    List<InferredOWLOntologyID> getAllSchemaOntologyVersions(RepositoryConnection repositoryConnection,
            URI schemaManagementGraph) throws OpenRDFException;
    
    List<URI> getAllValidMembers(InferredOWLOntologyID artifactID, URI conceptUri,
            RepositoryConnection repositoryConnection) throws OpenRDFException;
    
    /**
     * TODO: remove this from the API once the other method is used successfully
     * 
     * @deprecated use
     *             {@link com.github.podd.api.PoddSesameManager#getCardinalityValue(InferredOWLOntologyID, URI, URI, RepositoryConnection)}
     */
    @Deprecated
    Model getCardinality(InferredOWLOntologyID artifactID, URI objectUri, URI propertyUri,
            RepositoryConnection repositoryConnection) throws OpenRDFException;
    
    /**
     * Calculates the cardinality value for a given PODD object and property.
     * 
     * Possible output URIs represent the following cardinalities:
     * <ul>
     * <li>{@link PoddRdfConstants.PODD_BASE_CARDINALITY_EXACTLY_ONE} (Mandatory)</li>
     * <li>{@link PoddRdfConstants.PODD_BASE_CARDINALITY_ONE_OR_MANY} (Mandatory, can have multiple
     * values)</li>
     * <li>{@link PoddRdfConstants.PODD_BASE_CARDINALITY_ZERO_OR_MANY} (Optional, can have multiple
     * values)</li>
     * <li>{@link PoddRdfConstants.PODD_BASE_CARDINALITY_ZERO_OR_ONE} (Optional, the default)</li>
     * </ul>
     * 
     * <p>
     * For example, passing in <i>PoddTopObject</i> and property <i>hasLeadInstitution</i>, will
     * return {@link PoddRdfConstants.PODD_BASE_CARDINALITY_EXACTLY_ONE}.
     * </p>
     * <br>
     * <p>
     * <b>NOTE:</b> This method currently handles only Qualified Cardinality statements, which are
     * the only type found in PODD schema ontologies at present. However, as the property's value
     * type is ignored, the output is incomplete if a property has more than one type of possible
     * value with different cardinalities.
     * </p>
     * 
     * @param artifactID
     *            The artifact to which the object under consideration belongs
     * @param objectUri
     *            The object under consideration
     * @param propertyUri
     *            The property under consideration
     * @param repositoryConnection
     * @return a URI representing the cardinality value or NULL if no cardinality statements were
     *         found
     * @throws OpenRDFException
     * 
     * @since 03/05/2013
     */
    URI getCardinalityValue(InferredOWLOntologyID artifactID, URI objectUri, URI propertyUri,
            RepositoryConnection repositoryConnection) throws OpenRDFException;
    
    URI getCardinalityValue(URI objectUri, URI propertyUri, boolean findFromType,
            RepositoryConnection repositoryConnection, URI... contexts) throws OpenRDFException;
    
    /**
     * Returns current version details of an artifact ontology which has the given IRI as the
     * Ontology IRI or Version IRI.
     * 
     * @param ontologyIRI
     *            The IRI of the ontology to get current version info.
     * @param repositoryConnection
     * @param managementGraph
     *            The context of the Artifact Management Graph
     * @return An InferredOWLOntologyID containing details of the current managed version of the
     *         ontology.
     * @throws OpenRDFException
     * @throws UnmanagedArtifactIRIException
     *             If the given IRI does not refer to a managed artifact ontology
     * 
     * @since 04/01/2013
     */
    InferredOWLOntologyID getCurrentArtifactVersion(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
        UnmanagedArtifactIRIException;
    
    /**
     * Returns current version details of an ontology which has the given IRI as the Ontology IRI or
     * Version IRI.
     * 
     * @param ontologyIRI
     *            The IRI of the ontology to get current version info.
     * @param repositoryConnection
     * @param managementGraph
     *            The context of the Schema Management Graph
     * @return An InferredOWLOntologyID containing details of the current managed version of the
     *         ontology.
     * @throws OpenRDFException
     * @throws UnmanagedSchemaIRIException
     *             If the given IRI does not refer to a managed schema ontology
     * 
     * @since 18/12/2012
     */
    InferredOWLOntologyID getCurrentSchemaVersion(final IRI ontologyIRI,
            final RepositoryConnection repositoryConnection, final URI managementGraph) throws OpenRDFException,
        UnmanagedSchemaIRIException;
    
    /**
     * Retrieves the ontology IRIs for all import statements found in the given Repository
     * Connection.
     * 
     * @param ontologyID
     * @param repositoryConnection
     * @return A Set containing ontology IRIs for all import statements.
     * @throws OpenRDFException
     */
    Set<IRI> getDirectImports(final InferredOWLOntologyID ontologyID, final RepositoryConnection repositoryConnection)
        throws OpenRDFException;
    
    Set<IRI> getDirectImports(RepositoryConnection repositoryConnection, URI... contexts) throws OpenRDFException;
    
    /**
     * For a given PODD Object, this method finds all property values associated with it and also
     * links from others (e.g. parent objects) to this object. For example:
     * <ul>
     * <li>&lt;pObj_x&gt; rdfs:label "X" .</li>
     * <li>&lt;pObj_x&gt; rdfs:comment "This is a PODD process object" .</li>
     * <li>&lt;pObj_x&gt; poddScience:hasInvestigation &lt;pChild_y&gt; .</li>
     * <li>&lt;pArent&gt; poddScience:hasProcess &lt;pObj_x&gt; .</li>
     * </ul>
     * <br>
     * If the object URI is null, an empty {@link Model} is returned.
     * 
     * @param artifactID
     *            The PODD artifact to which this object belongs
     * @param objectUri
     *            The object whose details are sought.
     * @param repositoryConnection
     * @return A {@link Model} containing all statements about the object.
     * @throws OpenRDFException
     * 
     * @since 10/05/2013
     */
    Model getObjectData(InferredOWLOntologyID artifactID, URI objectUri, RepositoryConnection repositoryConnection)
        throws OpenRDFException;
    
    Model getObjectDetailsForDisplay(InferredOWLOntologyID artifactID, URI objectUri,
            RepositoryConnection repositoryConnection) throws OpenRDFException;
    
    Model getObjectDetailsForEdit(InferredOWLOntologyID artifactID, URI objectUri,
            RepositoryConnection repositoryConnection) throws OpenRDFException;
    
    PoddObjectLabel getObjectLabel(InferredOWLOntologyID ontologyID, URI objectUri,
            RepositoryConnection repositoryConnection) throws OpenRDFException;
    
    /**
     * <p>
     * For a given PODD Object type, this method returns meta-data about it which can be used to
     * render the object.
     * </p>
     * 
     * @param objectType
     *            The object type whose details are sought
     * @param includeDoNotDisplayProperties
     *            If true, properties that have been annotated as "Do Not Display" are also included
     * @param repositoryConnection
     * @param contexts
     *            The contexts from which metadata is to be retrieved
     * @return A {@link Model} containing statements which are useful for displaying this Object
     *         Type
     * @throws OpenRDFException
     * 
     * @since 10/05/2013
     */
    Model getObjectTypeMetadata(URI objectType, boolean includeDoNotDisplayProperties,
            RepositoryConnection repositoryConnection, URI... contexts) throws OpenRDFException;
    
    List<URI> getObjectTypes(InferredOWLOntologyID ontologyID, URI objectUri, RepositoryConnection repositoryConnection)
        throws OpenRDFException;
    
    /**
     * Returns a collection of ontologies managed in the given graph, optionally only returning the
     * current version.
     * 
     * @param onlyCurrentVersions
     *            If true, only the current version for each of the managed ontologies are returned.
     * @param ontologyManagementGraph
     *            The URI of the context in the repository containing the management information for
     *            the ontologies.
     * @return The collection of ontologies represented in the given management graph.
     */
    Collection<InferredOWLOntologyID> getOntologies(boolean onlyCurrentVersions,
            RepositoryConnection repositoryConnection, URI ontologyManagementGraph) throws OpenRDFException;
    
    /**
     * Retrieves from the given Repository Connection, an Ontology IRI which identifies an artifact.
     * 
     * @param repositoryConnection
     * @param context
     * @return The IRI of the ontology, or null if the Repository does not contain statements
     *         representing an ontology.
     * @throws OpenRDFException
     */
    IRI getOntologyIRI(final RepositoryConnection repositoryConnection, final URI context) throws OpenRDFException;
    
    /**
     * If the given IRI represents a version IRI of a schema ontology or an artifact, the Ontology
     * ID for this version is returned. Otherwise, null is returned.
     * 
     * @param versionIRI
     *            The IRI of the ontology to get current version info.
     * @param repositoryConnection
     * @param managementGraph
     *            The context of the Management Graph
     * @return An InferredOWLOntologyID containing details of the ontology, or NULL if the given IRI
     *         does not refer to the version IRI of a managed artifact/ontology
     * @throws OpenRDFException
     * 
     * @since 19/03/2013
     */
    InferredOWLOntologyID getOntologyVersion(IRI versionIRI, RepositoryConnection repositoryConnection,
            URI managementGraph) throws OpenRDFException;
    
    /**
     * If the given IRI represents a version IRI of a schema ontology, the Ontology ID for this
     * schema version is returned. If the given IRI represents an ontology IRI of a schema ontology,
     * the Ontology ID for the most current version of this schema ontology is returned.
     * 
     * 
     * @param schemaVersionIRI
     *            The IRI of the ontology to get current version info.
     * @param conn
     * @param schemaManagementGraph
     *            The context of the Schema Management Graph
     * @return An InferredOWLOntologyID containing details of the ontology.
     * @throws OpenRDFException
     * @throws UnmanagedSchemaIRIException
     *             If the given IRI does not refer to a managed schema ontology
     * 
     * @since 04/03/2013
     */
    InferredOWLOntologyID getSchemaVersion(IRI schemaVersionIRI, RepositoryConnection conn, URI schemaManagementGraph)
        throws OpenRDFException, UnmanagedSchemaIRIException;
    
    URI getTopObjectIRI(InferredOWLOntologyID ontologyIRI, RepositoryConnection repositoryConnection)
        throws OpenRDFException;
    
    List<URI> getTopObjects(InferredOWLOntologyID ontologyID, RepositoryConnection repositoryConnection)
        throws OpenRDFException;
    
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
     * @param artifactID
     *            The artifact to which this object belongs
     * @param objectUri
     *            The object whose properties are sought
     * @param excludeContainsProperties
     *            Whether to exclude sub-properties of "poddBase:contains" property
     * @param repositoryConnection
     * @return A List containing URIs of sorted properties about the object
     * 
     * @throws OpenRDFException
     */
    List<URI> getWeightedProperties(final InferredOWLOntologyID artifactID, final URI objectUri,
            final boolean excludeContainsProperties, final RepositoryConnection repositoryConnection)
        throws OpenRDFException;
    
    /**
     * Returns true if the combination of the Ontology IRI and the Version IRI in the given
     * ontologyID were previously published.
     * 
     * @param ontologyID
     * @param repositoryConnection
     * @return
     * @throws OpenRDFException
     */
    boolean isPublished(OWLOntologyID ontologyID, RepositoryConnection repositoryConnection, final URI context)
        throws OpenRDFException;
    
    /**
     * Carries out a case-insensitive search for objects whose labels match a given term. The search
     * is carried out in the specified artifact's concrete triples and the ontologies imported by
     * that artifact. An optional array of URIs can be used to limit the RDF types of objects to
     * match. <br>
     * 
     * TODO: Replace all invocations of this method with the overloaded method which takes in an
     * array of contexts instead of an artifact ID.
     * 
     * @param searchTerm
     *            A String term which is searched for in the RDF:Labels
     * @param artifactID
     *            The artifact whose concrete graph and its imported ontologies are searched
     * @param limit
     * @param offset
     * @param repositoryConnection
     * @param searchTypes
     *            The types (i.e. RDF:Type) of results to match with the search term
     * @return A {@link Model} containing the URI and Label of each matching object.
     * 
     */
    Model searchOntologyLabels(String searchTerm, InferredOWLOntologyID artifactID, int limit, int offset,
            final RepositoryConnection repositoryConnection, URI... searchTypes) throws OpenRDFException;
    
    /**
     * Carries out a case-insensitive search for objects whose labels match a given term. The search
     * is carried out in the specified contexts. An optional array of URIs can be used to limit the
     * RDF types of objects to match. <br>
     * 
     * @param searchTerm
     *            A String term which is searched for in the RDF:Labels
     * @param searchTypes
     *            The types (i.e. RDF:Type) of results to match with the search term
     * @param limit
     * @param offset
     * @param repositoryConnection
     * @param contexts
     * @return
     * @throws OpenRDFException
     */
    Model searchOntologyLabels(String searchTerm, URI[] searchTypes, int limit, int offset,
            RepositoryConnection repositoryConnection, URI... contexts) throws OpenRDFException;
    
    /**
     * Sets the given Ontology IRI to be published. This restricts the ability to publish the
     * ontology again.
     * 
     * @param ontologyID
     *            The {@link InferredOWLOntologyID} identifying the ontology that needs to be
     *            published
     * @param repositoryConnection
     * @param artifactManagementGraph
     * @throws OpenRDFException
     * @throws UnmanagedArtifactIRIException
     *             If this is not a managed ontology
     */
    void setPublished(final InferredOWLOntologyID ontologyID, final RepositoryConnection repositoryConnection,
            final URI artifactManagementGraph) throws OpenRDFException, UnmanagedArtifactIRIException;
    
    /**
     * This method adds information to the Schema Ontology management graph, and updates the links
     * for the current version for both the ontology and the inferred ontology.
     * 
     * @param nextOntologyID
     *            The ontology ID that contains the information about the ontology, including the
     *            inferred ontology IRI.
     * @param updateCurrent
     *            If true, will update the current version if it exists. If false it will only add
     *            the current version if it does not exist. Set this to false when only inferred
     *            ontology information needs to be added. This will never remove statements related
     *            to previous versions of schema ontologies.
     * @param repositoryConnection
     * @param context
     * @throws OpenRDFException
     */
    void updateCurrentManagedSchemaOntologyVersion(InferredOWLOntologyID nextOntologyID, boolean updateCurrent,
            RepositoryConnection repositoryConnection, URI context) throws OpenRDFException;
    
    /**
     * This method adds information to the PODD artifact management graph, and updates the links for
     * the current version for both the ontology and the inferred ontology.
     * 
     * @param nextOntologyID
     *            The ontology ID that contains the information about the ontology, including the
     *            inferred ontology IRI.
     * @param updateCurrentAndRemovePrevious
     *            If true, will update the current version if it exists, and remove all statements
     *            relating to previous versions. If false it will only add the current version if it
     *            does not exist.
     * @param repositoryConnection
     * @param context
     * @throws OpenRDFException
     */
    void updateManagedPoddArtifactVersion(InferredOWLOntologyID nextOntologyID, boolean updateCurrentAndRemovePrevious,
            RepositoryConnection repositoryConnection, URI context) throws OpenRDFException;
    
}
