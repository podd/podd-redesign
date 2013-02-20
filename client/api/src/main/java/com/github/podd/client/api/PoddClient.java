/**
 * 
 */
package com.github.podd.client.api;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.openrdf.rio.RDFFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyID;

/**
 * An interface defining the operations that are currently implemented by the PODD Web Services.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddClient
{
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
     * @return An {@link OWLOntologyID} object containing the details of the updated artifact.
     */
    OWLOntologyID appendArtifact(OWLOntologyID ontologyId, InputStream partialInputStream, RDFFormat format)
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
     *            The OWLOntologyID of the artifact to attach the file reference to.
     * @param objectIRI
     *            The IRI of the object to attach the file reference to.
     * @param label
     *            A label to attach to the file reference.
     * @param repositoryAlias
     *            The alias of the repository that the file is located in.
     * @param filePathInRepository
     *            The path inside of the repository that can be used to locate the file.
     * @return An {@link OWLOntologyID} object containing the details of the updated artifact.
     */
    OWLOntologyID attachFileReference(OWLOntologyID ontologyId, IRI objectIRI, String label, String repositoryAlias,
            String filePathInRepository) throws PoddClientException;
    
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
    boolean deleteArtifact(OWLOntologyID ontologyId) throws PoddClientException;
    
    /**
     * Submits a request to the PODD Get Artifact service to download the artifact identified by the
     * given {@link OWLOntologyID}, optionally including a version IRI if it is specifically known.
     * <p>
     * If the version is not currently available, the latest version will be returned.
     * 
     * @param artifactId
     *            The OWLOntologyID of the artifact to be downloaded, including version as necessary
     *            to fetch old versions.
     * @param outputStream
     *            The {@link OutputStream} to download the artifact to.
     * @param format
     *            The format of the RDF information to be downloaded to the output stream.
     */
    void downloadArtifact(OWLOntologyID artifactId, OutputStream outputStream, RDFFormat format);
    
    /**
     * Gets the base server URL to use when submitting requests using this client.
     * 
     * @return The server URL. For example, <tt>http://localhost:8080/podd/</tt> if the server is
     *         hosted locally. Returns null if a server URL has not been set.
     */
    String getPoddServerUrl();
    
    /**
     * Returns the current login status.
     * 
     * @return True if the client was logged in after the last request, and false otherwise.
     */
    boolean isLoggedIn();
    
    /**
     * 
     * @return A list of Strings identifying the possible values for the repository alias in calls
     *         to {@link #attachFileReference(IRI, String, String)}.
     */
    List<String> listFileReferenceRepositories() throws PoddClientException;
    
    /**
     * 
     * @return A list of {@link OWLOntologyID}s identifying the artifacts that the user has access
     *         to which are published. This may include artifacts that the user cannot modify or
     *         fork.
     */
    List<OWLOntologyID> listPublishedArtifacts() throws PoddClientException;
    
    /**
     * 
     * @return A list of {@link OWLOntologyID}s identifying the artifacts that the user has access
     *         to which are unpublished.
     */
    List<OWLOntologyID> listUnpublishedArtifacts() throws PoddClientException;
    
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
     *            The {@link OWLOntologyID} of the unpublished artifact that is to be published.
     * @return The {@link OWLOntologyID} of the artifact that was published. Artifacts may be given
     *         a different IRI after they are published, to distinguish them from the previously
     *         unpublished artifact.
     */
    OWLOntologyID publishArtifact(OWLOntologyID ontologyId) throws PoddClientException;
    
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
     * @return The {@link OWLOntologyID} of the artifact after it has been unpublished. Artifacts
     *         may be given a different IRI after they unpublished, to distinguish them from the
     *         previously available artifact.
     */
    OWLOntologyID unpublishArtifact(OWLOntologyID ontologyId) throws PoddClientException;
    
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
     * @return An {@link OWLOntologyID} object containing the details of the updated artifact.
     */
    OWLOntologyID updateArtifact(OWLOntologyID ontologyId, InputStream fullInputStream, RDFFormat format)
        throws PoddClientException;
    
    /**
     * Submits a request to the PODD Load Artifact service.
     * 
     * @param input
     *            The {@link InputStream} containing the artifact to load.
     * @param format
     *            The format of the RDF triples in the given InputStream.
     * @return An {@link OWLOntologyID} object containing the details of the loaded artifact. The
     *         {@link OWLOntologyID#getOntologyIRI()} method can be used to get the artifact IRI for
     *         future requests, while the {@link OWLOntologyID#getVersionIRI()} method can be used
     *         to get the version IRI to determine if there have been changes to the ontology in
     *         future.
     */
    OWLOntologyID uploadNewArtifact(InputStream input, RDFFormat format) throws PoddClientException;
}