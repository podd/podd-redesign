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
package com.github.podd.client.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Model;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;

import com.github.ansell.restletutils.RestletUtilRole;
import com.github.podd.api.DanglingObjectPolicy;
import com.github.podd.api.DataReferenceVerificationPolicy;
import com.github.podd.api.data.DataReference;
import com.github.podd.utils.InferredOWLOntologyID;
import com.github.podd.utils.PoddUser;

/**
 * An interface defining the operations that are currently implemented by the PODD Web Services.
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddClient
{
    /**
     * Fetch all of the properties for the given object URI.
     */
    public static final String TEMPLATE_SPARQL_BY_URI = new StringBuilder()
            .append("CONSTRUCT { ?object ?predicate ?value . }").append(" WHERE { ?object ?predicate ?value . }")
            .append(" VALUES ( ?object ) { ( %s ) }").toString();
    /**
     * Fetch all of the properties for the given objects under the given parent with the given type.
     */
    public static final String TEMPLATE_SPARQL_BY_TYPE_AND_PARENT_ALL_PROPERTIES =
            new StringBuilder()
                    .append("CONSTRUCT { ?parent ?parentPredicate ?object . ?object a ?type . ?object ?predicate ?label . }")
                    .append(" WHERE { ?parent ?parentPredicate ?object . ?object a ?type . OPTIONAL { ?object ?predicate ?label . } }")
                    .append(" VALUES (?parent ?parentPredicate ?type ) { ( %s %s %s ) }").toString();
    
    /**
     * Fetch type and label and barcode statements for the given object type.
     */
    public static final String TEMPLATE_SPARQL_BY_TYPE_WITH_LABEL = new StringBuilder()
            .append("CONSTRUCT { ?object a ?type . ")
            .append(" ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . ")
            .append(" ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . } ")
            .append(" WHERE { ?object a ?type . ")
            .append(" OPTIONAL { ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
            .append(" OPTIONAL { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . } }")
            .append(" VALUES (?type) { ( %s ) }").toString();
    
    /**
     * Fetch all of the properties for the given objects with the given type
     */
    public static final String TEMPLATE_SPARQL_BY_TYPE_ALL_PROPERTIES = new StringBuilder()
            .append("CONSTRUCT { ?object a ?type . ?object ?predicate ?value . }")
            .append(" WHERE { ?object a ?type . ?object ?predicate ?value . }").append(" VALUES (?type) { ( %s ) }")
            .toString();
    
    public static final String TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS =
            new StringBuilder()
                    .append("CONSTRUCT { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . }")
                    .append(" WHERE { ?object a ?type . ?object <http://www.w3.org/2000/01/rdf-schema#label> ?label . FILTER(STRSTARTS(?label, \"%s\")) }")
                    .append(" VALUES (?type) { ( %s ) }").toString();
    
    public static final String TEMPLATE_SPARQL_BY_BARCODE_STRSTARTS =
            new StringBuilder()
                    .append("CONSTRUCT { ?object a ?type . ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . ?object ?property ?value . }")
                    .append(" WHERE { ?object a ?type . ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . FILTER(STRSTARTS(?barcode, \"%s\")). ?object ?property ?value . }")
                    .append(" VALUES (?type) { ( %s ) }").toString();
    
    public static final String TEMPLATE_SPARQL_BY_BARCODE_MATCH_NO_TYPE =
            new StringBuilder()
                    .append("CONSTRUCT { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . ?object ?property ?value . }")
                    .append(" WHERE { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . FILTER(STR(?barcode) = \"%s\"). ?object ?property ?value . }")
                    .toString();
    
    public static final String TEMPLATE_SPARQL_BY_BARCODE_MATCH_NO_TYPE_NO_BARCODE =
            new StringBuilder()
                    .append("CONSTRUCT { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . ?object ?property ?value . }")
                    .append(" WHERE { ?object <http://purl.org/podd/ns/poddScience#hasBarcode> ?barcode . ?object ?property ?value . }")
                    .toString();
    
    public static final String TEMPLATE_SPARQL_CONTAINERS_TO_MATERIAL_AND_GENOTYPE =
            new StringBuilder()
                    .append("CONSTRUCT { ?container <http://purl.org/podd/ns/poddScience#hasMaterial> ?material . ?material <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?material ?materialProperty ?materialValue . ?genotype ?property ?value . }")
                    .append(" WHERE { ?container <http://purl.org/podd/ns/poddScience#hasMaterial> ?material . ?material <http://purl.org/podd/ns/poddScience#refersToGenotype> ?genotype . ?material ?materialProperty ?materialValue . ?genotype ?property ?value . }")
                    .append(" VALUES (?container) { %s }").toString();
    
    /**
     * NOTE: Both the first and second arguments are the predicate, the first being the mapped
     * predicate, and the second being the original predicate.
     */
    public static final String TEMPLATE_SPARQL_BY_TYPE_LABEL_STRSTARTS_PREDICATE = new StringBuilder()
            .append("CONSTRUCT { ?object a ?type . ?object %s ?label . }")
            .append(" WHERE { ?object a ?type . ?object %s ?label . FILTER(STRSTARTS(?label, \"%s\")) }")
            .append(" VALUES (?type) { ( %s ) }").toString();
    
    /**
     * NOTE: Both the first and second arguments are the predicate, the first being the mapped
     * predicate, and the second being the original predicate.
     */
    public static final String TEMPLATE_SPARQL_BY_PREDICATE = new StringBuilder()
            .append("CONSTRUCT { ?object %s ?property . }").append(" WHERE { ?object %s ?property . }").toString();
    
    public static final String TEMPLATE_SPARQL_TRAY_POT_NUMBER_TO_BARCODE = new StringBuilder().append("CONSTRUCT { ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasPotNumberTray> ?potNumberTray . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasPotNumber> ?potNumberOverall . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasBarcode> ?potBarcode . }")
            .append(" WHERE { ?tray <http://purl.org/podd/ns/poddScience#hasBarcode> ?trayBarcode . ")
            .append(" ?tray <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasPotNumberTray> ?potNumberTray . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasPotNumber> ?potNumberOverall . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasBarcode> ?potBarcode . ")
            .append(" FILTER(STR(?trayBarcode) = \"%s\") }").toString();
    
    public static final String TEMPLATE_SPARQL_TRAY_POT_NUMBER_TO_BARCODE_ALL = new StringBuilder()
            .append("CONSTRUCT { ").append(" ?tray a ?trayType . ")
            .append(" ?tray <http://purl.org/podd/ns/poddScience#hasBarcode> ?trayBarcode . ")
            .append(" ?tray <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ").append(" ?pot a ?potType . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasPotNumberTray> ?potNumberTray . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasPotNumber> ?potNumberOverall . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasBarcode> ?potBarcode . }").append(" WHERE { ")
            .append(" ?tray a ?trayType . ")
            .append(" ?tray <http://purl.org/podd/ns/poddScience#hasBarcode> ?trayBarcode . ")
            .append(" ?tray <http://purl.org/podd/ns/poddScience#hasPot> ?pot . ").append(" ?pot a ?potType . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasPotNumberTray> ?potNumberTray . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasPotNumber> ?potNumberOverall . ")
            .append(" ?pot <http://purl.org/podd/ns/poddScience#hasBarcode> ?potBarcode . }").toString();
    
    /**
     * Adds the given role for the given user to the given artifact
     *
     * @param userIdentifier
     * @param role
     * @param artifact
     * @throws PoddClientException
     *             If there is an error setting the role for the given user.
     */
    void addRole(String userIdentifier, RestletUtilRole role, InferredOWLOntologyID artifact)
        throws PoddClientException;
    
    /**
     * Submits a request to the PODD Edit Artifact service to append to the artifact using the RDF
     * triples that are contained in the given {@link InputStream}.
     * <p>
     * If the given ontologyId contains a version IRI and the version is out of date, a
     * PoddClientException may be thrown if the server refuses to complete the operation due to the
     * version being out of date. In these cases the ontology would need to be manually merged, and
     * the update would need to be attempted again.
     *
     * @param ontologyIRI
     *            The IRI of the Artifact to update.
     * @param format
     *            The format of the RDF triples in the given InputStream.
     * @param partialInputStream
     *            The partial set of RDF triples serialised into an InputStream in the given format
     *            that will be appended to the given artifact.
     * @return An {@link InferredOWLOntologyID} object containing the details of the updated
     *         artifact.
     */
    InferredOWLOntologyID appendArtifact(InferredOWLOntologyID ontologyId, InputStream partialInputStream,
            RDFFormat format) throws PoddClientException;
    
    InferredOWLOntologyID appendArtifact(InferredOWLOntologyID ontologyId, InputStream partialInputStream,
            RDFFormat format, DanglingObjectPolicy danglingObjectPolicy,
            DataReferenceVerificationPolicy dataReferenceVerificationPolicy) throws PoddClientException;
    
    /**
     * Appends multiple artifacts in PODD.
     *
     * @param uploadQueue
     *            A Map containing the keys for the artifacts, and Models containing the appended
     *            content for each of the artifacts.
     * @return A map from the original keys to the new artifact keys after the changes.
     * @throws PoddClientException
     *             If an error occurred.
     */
    Map<InferredOWLOntologyID, InferredOWLOntologyID> appendArtifacts(Map<InferredOWLOntologyID, Model> uploadQueue)
        throws PoddClientException;
    
    /**
     * Submits a request to the PODD File Reference Attachment service to attach a file reference
     * from a registered repository into the artifact as a child of the given object IRI.
     * <p>
     * If the given ontologyId contains a version IRI and the version is out of date, a
     * PoddClientException may be thrown if the server refuses to complete the operation due to the
     * version being out of date. In these cases the ontology would need to be manually merged, and
     * the update would need to be attempted again.
     *
     * @param ontologyId
     *            The {@link InferredOWLOntologyID} of the artifact to attach the file reference to.
     * @param objectIRI
     *            The IRI of the object to attach the file reference to.
     * @param label
     *            A label to attach to the file reference.
     * @param repositoryAlias
     *            The alias of the repository that the file is located in.
     * @param filePathInRepository
     *            The path inside of the repository that can be used to locate the file.
     * @return An {@link InferredOWLOntologyID} object containing the details of the updated
     *         artifact.
     */
    InferredOWLOntologyID attachDataReference(DataReference ref) throws PoddClientException;
    
    /**
     * Creates a new PoddUser using the details in the given PoddUser.
     *
     * @param user
     *            The user to create.
     * @return An instance of PoddUser containing the actual details of the created user, except for
     *         the password.
     * @throws PoddClientException
     */
    PoddUser createUser(PoddUser user) throws PoddClientException;
    
    /**
     * Submits a request to the PODD Delete Artifact service to delete the artifact identified by
     * the given IRI.
     * <p>
     * If the given ontologyId contains a version IRI and the version is out of date, a
     * PoddClientException may be thrown if the server refuses to complete the operation due to the
     * version being out of date. In these cases the ontology deletion would need to be attempted
     * again using the up to date version, or alternatively, by omitting the version IRI.
     *
     * @param ontologyId
     *            The OWLOntologyID of the artifact to delete.
     * @return True if the artifact was deleted and false otherwise.
     */
    boolean deleteArtifact(InferredOWLOntologyID ontologyId) throws PoddClientException;
    
    /**
     * Performs a CONSTRUCT or DESCRIBE SPARQL query on the given artifact.
     *
     * @param queryString
     *            The CONSTRUCT or DESCRIBE SPARQL query on the given artifact.
     * @param artifacts
     *            The PODD artifacts to perform the query on.
     * @return A {@link Model} containing the results of the SPARQL query.
     * @throws PoddClientException
     *             If an error occurred.
     */
    Model doSPARQL(String queryString, Collection<InferredOWLOntologyID> artifacts) throws PoddClientException;
    
    /**
     * Submits a request to the PODD Get Artifact service to download the artifact identified by the
     * given {@link InferredOWLOntologyID}, optionally including a version IRI if it is specifically
     * known.
     * <p>
     * If the version is not currently available, the latest version will be returned.
     *
     * @param artifactId
     *            The {@link InferredOWLOntologyID} of the artifact to be downloaded, including
     *            version as necessary to fetch old versions.
     * @return A model containing the RDF statements
     * @throws PoddClientException
     *             If the artifact could not be downloaded for any reason
     */
    Model downloadArtifact(InferredOWLOntologyID artifactId) throws PoddClientException;
    
    /**
     * Submits a request to the PODD Get Artifact service to download the artifact identified by the
     * given {@link InferredOWLOntologyID}, optionally including a version IRI if it is specifically
     * known.
     * <p>
     * If the version is not currently available, the latest version will be returned.
     *
     * @param artifactId
     *            The {@link InferredOWLOntologyID} of the artifact to be downloaded, including
     *            version as necessary to fetch old versions.
     * @param outputStream
     *            The {@link OutputStream} to download the artifact to.
     * @param format
     *            The format of the RDF information to be downloaded to the output stream.
     * @throws PoddClientException
     *             If the artifact could not be downloaded for any reason
     */
    void downloadArtifact(InferredOWLOntologyID artifactId, OutputStream outputStream, RDFFormat format)
        throws PoddClientException;
    
    /**
     * Return all barcodes and all statements linked directly to objects that have barcodes assigned
     * to them.
     *
     * @param artifacts
     *            An optional list of artifacts which are to be searched.
     * @return A {@link Model} containing the RDF statements which describe all of the matched
     *         objects.
     */
    Model getAllBarcodes(Collection<InferredOWLOntologyID> artifacts);
    
    /**
     * Returns RDF statements containing all of the directly linked statements from the URI.
     *
     * @param object
     *            The URI of the object to search for. Must not be null.
     * @param artifacts
     *            An optional list of artifacts which are to be searched.
     * @return A {@link Model} containing the RDF statements which describe the matching object.
     * @throws PoddClientException
     *             If there is an exception while executing the query.
     */
    Model getObjectByURI(URI object, Collection<InferredOWLOntologyID> artifacts) throws PoddClientException;
    
    /**
     * Returns RDF statements containing the types and labels for all objects in the given artifacts
     * with the given types. If there are no artifacts specified then all accessible artifacts will
     * be searched. The type is the fully inferred type for the object, not just its concrete types.
     *
     * @param type
     *            The URI with the RDF Type to search for. Must not be null.
     * @param artifacts
     *            An optional list of artifacts which are to be searched.
     * @return A {@link Model} containing the RDF statements which describe the matching objects.
     * @throws PoddClientException
     *             If there is an exception while executing the query.
     */
    Model getObjectsByType(URI type, Collection<InferredOWLOntologyID> artifacts) throws PoddClientException;
    
    /**
     * Returns RDF statements containing the predicate.
     *
     * @param predicate
     *            The URI with the RDF predicate to search for. Must not be null.
     * @param artifacts
     *            An optional list of artifacts which are to be searched.
     * @return A {@link Model} containing the RDF statements which describe the matching objects.
     * @throws PoddClientException
     *             If there is an exception while executing the query.
     */
    Model getObjectsByPredicate(URI predicate, Collection<InferredOWLOntologyID> artifacts) throws PoddClientException;
    
    /**
     * Returns RDF statements containing the types and labels for all objects in the given artifacts
     * with the given types linked to from the given parent with the given predicate. If there are
     * no artifacts specified then all accessible artifacts will be searched. The type is the fully
     * inferred type for the object, not just its concrete types, and the parentPredicate may be a
     * super-property of the concrete property that was used.
     *
     * @param type
     *            The URI with the RDF Type to search for. Must not be null.
     * @param labelPrefix
     *            The string which must start the {@link RDFS#LABEL} for the object for it to be
     *            matched.
     * @param artifacts
     *            An optional list of artifacts which are to be searched.
     * @return A {@link Model} containing the RDF statements which describe the matching objects.
     * @throws PoddClientException
     *             If there is an exception while executing the query.
     */
    Model getObjectsByTypeAndParent(URI parent, URI parentPredicate, URI type,
            Collection<InferredOWLOntologyID> artifacts) throws PoddClientException;
    
    /**
     * Returns RDF statements containing the types and labels for all objects in the given artifacts
     * with the given types, whose labels start with the given prefix. If there are no artifacts
     * specified then all accessible artifacts will be searched. The type is the fully inferred type
     * for the object, not just its concrete types.
     *
     * @param type
     *            The URI with the RDF Type to search for. Must not be null.
     * @param labelPrefix
     *            The string which must start the {@link RDFS#LABEL} for the object for it to be
     *            matched.
     * @param artifacts
     *            An optional list of artifacts which are to be searched.
     * @return A {@link Model} containing the RDF statements which describe the matching objects.
     * @throws PoddClientException
     *             If there is an exception while executing the query.
     */
    Model getObjectsByTypeAndPrefix(URI type, String labelPrefix, Collection<InferredOWLOntologyID> artifacts)
        throws PoddClientException;
    
    /**
     * Gets the base server URL to use when submitting requests using this client.
     *
     * @return The server URL. For example, <tt>http://localhost:8080/podd/</tt> if the server is
     *         hosted locally. Returns null if a server URL has not been set.
     */
    String getPoddServerUrl();
    
    /**
     *
     * @param userIdentifier
     *            The user identifier to fetch details for, or null to fetch the current user
     *            details.
     * @return A {@link PoddUser} object containing the relevant details for the user.
     * @throws PoddClientException
     *             If the user is not accessible, including if the user does not exist.
     */
    PoddUser getUserDetails(String userIdentifier) throws PoddClientException;
    
    /**
     * Returns the current login status.
     *
     * @return True if the client was logged in after the last request, and false otherwise.
     */
    boolean isLoggedIn();
    
    /**
     * Lists the artifacts that are accessible and returns the details as a {@link Model}.
     *
     * @param published
     *            If true, requests are made for published artifacts. If this is false, unpublished
     *            must NOT be false.
     * @param unpublished
     *            If true, requests are made for the unpublished artifacts accessible to the current
     *            user. If this is false, published must NOT be false.
     * @return A Model containing RDF statements describing the artifact.
     * @throws PoddClientException
     *             If an error occurred.
     */
    Model listArtifacts(boolean published, boolean unpublished) throws PoddClientException;
    
    /**
     *
     * @return A list of Strings identifying the possible values for the repository alias in calls
     *         to {@link #attachFileReference(IRI, String, String)}.
     */
    List<String> listDataReferenceRepositories() throws PoddClientException;
    
    /**
     *
     * @return A map of the {@link InferredOWLOntologyID}s to top object labels, identifying the
     *         artifacts that the user has access to which are published. This may include artifacts
     *         that the user cannot modify or fork.
     */
    Set<PoddArtifact> listPublishedArtifacts() throws PoddClientException;
    
    /**
     * List the roles that have been assigned to the given artifact.
     *
     * @param artifactId
     *            The {@link InferredOWLOntologyID} identifying an artifact to fetch roles for.
     *
     * @return A map of {@link RestletUtilRole}s identifying PODD roles attached to the given
     *         artifact to users who have each role.
     * @throws PoddClientException
     */
    Map<RestletUtilRole, Collection<String>> listRoles(InferredOWLOntologyID artifactId) throws PoddClientException;
    
    /**
     * List the roles that have been assigned to the given user, or the currently logged in user if
     * the user is not specified.
     *
     * @param userIdentifier
     *            If not null, specifies a specific user to request information about.
     *
     * @return A map of {@link RestletUtilRole}s identifying roles that have been given to the user,
     *         optionally to artifacts that the role maps to for this user.
     * @throws PoddClientException
     */
    Map<RestletUtilRole, Collection<URI>> listRoles(String userIdentifier) throws PoddClientException;
    
    /**
     *
     * @return A map of the {@link InferredOWLOntologyID}s to labels, identifying the artifacts that
     *         the user has access to which are unpublished.
     */
    Set<PoddArtifact> listUnpublishedArtifacts() throws PoddClientException;
    
    /**
     *
     * @return A list of the current users registered with the system, masked by the abilities of
     *         the current user to view each users existence. If the current user is a repository
     *         administrator they should be able to view all users. Some other roles may only be
     *         able to see some other users.
     */
    List<PoddUser> listUsers() throws PoddClientException;
    
    /**
     * Submits a request to the PODD Login service to login the user with the given username and
     * password.
     * <p>
     * Once the user is logged in, future queries using this client, prior to calling the logout
     * method, will be authenticated as the given user, barring any session timeouts that may occur.
     * <p>
     * If the given user is already logged in, this method may return true immediately without
     * reauthentication.
     *
     * @param username
     *            The username to submit to the login service.
     * @param password
     *            A character array containing the password to submit to the login service.
     * @return True if the user was successfully logged in and false otherwise.
     */
    boolean login(String username, String password) throws PoddClientException;
    
    /**
     * Submits a request to the PODD Logout service to logout the user and close the session.
     *
     * @return True if the user was successfully logged out and false otherwise.
     */
    boolean logout() throws PoddClientException;
    
    /**
     * Submits a request to the PODD Publish Artifact service to publish an artifact that was
     * previously unpublished.
     * <p>
     * If the given ontologyId contains a version IRI and the version is out of date, a
     * PoddClientException may be thrown if the server refuses to complete the operation due to the
     * version being out of date. In these cases the ontology would need to be manually merged, and
     * the publish would need to be attempted again.
     *
     * @param ontologyId
     *            The {@link InferredOWLOntologyID} of the unpublished artifact that is to be
     *            published.
     * @return The {@link InferredOWLOntologyID} of the artifact that was published. Artifacts may
     *         be given a different IRI after they are published, to distinguish them from the
     *         previously unpublished artifact.
     */
    InferredOWLOntologyID publishArtifact(InferredOWLOntologyID ontologyId) throws PoddClientException;
    
    /**
     * Removes the given role for the given user to the given artifact.
     *
     * @param userIdentifier
     * @param role
     * @param artifact
     * @throws PoddClientException
     *             If there is an error removing the role for the given user.
     */
    void removeRole(String userIdentifier, RestletUtilRole role, InferredOWLOntologyID artifact)
        throws PoddClientException;
    
    /**
     * Sets the base server URL to use when submitting requests using this client.
     *
     * @param serverUrl
     *            The server URL. For example, <tt>http://localhost:8080/podd/</tt> if the server is
     *            hosted locally.
     */
    void setPoddServerUrl(String serverUrl);
    
    /**
     * Submits a request to the PODD Unpublish Artifact service to unpublish an artifact that was
     * previously published.
     * <p>
     * If the given ontologyId contains a version IRI and the version is out of date, a
     * PoddClientException will be thrown, as the published artifact must have an accurate version
     * to ensure consistency. To avoid this, the operation may be attempted omitting the version
     * IRI.
     *
     * @param ontologyId
     * @return The {@link InferredOWLOntologyID} of the artifact after it has been unpublished.
     *         Artifacts may be given a different IRI after they unpublished, to distinguish them
     *         from the previously available artifact.
     */
    InferredOWLOntologyID unpublishArtifact(InferredOWLOntologyID ontologyId) throws PoddClientException;
    
    /**
     * Submits a request to the PODD Edit Artifact service to update the entire artifact, replacing
     * the existing content with the content in the given {@link InputStream}.
     * <p>
     * If the given ontologyId contains a version IRI and the version is out of date, a
     * PoddClientException may be thrown if the server refuses to complete the operation due to the
     * version being out of date. In these cases the ontology would need to be manually merged, and
     * the update would need to be attempted again.
     *
     * @param ontologyId
     *            The OWLOntologyID of the Artifact to update.
     * @param format
     *            The format of the RDF triples in the given InputStream.
     * @param fullInputStream
     *            The full set of RDF triples serialised into the InputStream in the given format
     *            that will be used to update the given artifact.
     * @return An {@link InferredOWLOntologyID} object containing the details of the updated
     *         artifact.
     */
    InferredOWLOntologyID updateArtifact(InferredOWLOntologyID ontologyId, InputStream fullInputStream, RDFFormat format)
        throws PoddClientException;
    
    /**
     * Submits a request to the PODD Load Artifact service.
     *
     * @param input
     *            The {@link InputStream} containing the artifact to load.
     * @param format
     *            The format of the RDF triples in the given InputStream.
     * @return An {@link InferredOWLOntologyID} object containing the details of the loaded
     *         artifact. The {@link InferredOWLOntologyID#getOntologyIRI()} method can be used to
     *         get the artifact IRI for future requests, while the
     *         {@link InferredOWLOntologyID#getVersionIRI()} method can be used to get the version
     *         IRI to determine if there have been changes to the ontology in future.
     */
    InferredOWLOntologyID uploadNewArtifact(InputStream input, RDFFormat format) throws PoddClientException;
    
    InferredOWLOntologyID uploadNewArtifact(InputStream input, RDFFormat format,
            DanglingObjectPolicy danglingObjectPolicy, DataReferenceVerificationPolicy dataReferenceVerificationPolicy)
        throws PoddClientException;
    
    /**
     * Submits a request to the PODD Load Artifact service.
     *
     * @param model
     *            The {@link Model} containing the artifact to load.
     * @return An {@link InferredOWLOntologyID} object containing the details of the loaded
     *         artifact. The {@link InferredOWLOntologyID#getOntologyIRI()} method can be used to
     *         get the artifact IRI for future requests, while the
     *         {@link InferredOWLOntologyID#getVersionIRI()} method can be used to get the version
     *         IRI to determine if there have been changes to the ontology in future.
     */
    InferredOWLOntologyID uploadNewArtifact(Model model) throws PoddClientException;
    
    Model getObjectsByTypePredicateAndPrefix(URI type, URI predicate, String labelPrefix,
            Collection<InferredOWLOntologyID> artifacts) throws PoddClientException;
    
    /**
     * Try to automatically login using the properties defined in poddclient.properties.
     *
     * @return True if the login was successful and false if it was unsuccessful.
     * @throws PoddClientException
     *             If there was an exception accessing PODD.
     */
    boolean autologin() throws PoddClientException;
    
    Model getObjectsByTypeAndBarcode(URI type, String barcode, Collection<InferredOWLOntologyID> artifacts)
        throws PoddClientException;
    
    Model getObjectsByBarcode(String barcode, Collection<InferredOWLOntologyID> artifacts) throws PoddClientException;
}
