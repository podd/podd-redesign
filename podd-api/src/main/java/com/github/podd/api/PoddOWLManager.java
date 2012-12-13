/**
 * 
 */
package com.github.podd.api;

import java.io.IOException;
import java.util.List;

import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.io.OWLOntologyDocumentSource;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.github.podd.exception.PoddException;
import com.github.podd.exception.PublishArtifactException;
import com.github.podd.utils.InferredOWLOntologyID;

/**
 * Manages interactions with OWLAPI for PODD.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface PoddOWLManager
{
    /**
     * Loads and caches the given schema ontology in memory from a Repository.
     * 
     * @param ontologyID
     * @param conn
     * @throws OpenRDFException
     */
    void cacheSchemaOntology(InferredOWLOntologyID ontologyID, RepositoryConnection conn) throws OpenRDFException;
    
    /**
     * Creates a reasoner over the given OWLOntology
     * 
     * @param nextOntology
     * @return
     */
    OWLReasoner createReasoner(OWLOntology nextOntology);
    
    /**
     * Generates a new unique IRI for inferred statements attached to the given OWLOntologyID, and
     * returns the result inside of a new InferredOWLOntologyID object.
     * 
     * @param ontologyID
     * @return
     */
    InferredOWLOntologyID generateInferredOntologyID(OWLOntologyID ontologyID);
    
    /**
     * 
     * @param ontologyIRI
     *            The IRI of the ontology to get.
     * @return The managed current version of the ontology with ontologyIRI as either the Ontology
     *         IRI, or the Version IRI.
     */
    OWLOntologyID getCurrentVersion(IRI ontologyIRI);
    
    /**
     * NOTE: Restrict usage of this method, as it will always fetch the entire OWLOntology into
     * memory. Use getCurrentVersion or getVersions in most cases.
     * 
     * @param ontologyID
     *            The full OWLOntologyID, containing both Ontology IRI and Version IRI for the
     *            ontology to fetch.
     * @return An instance of OWLOntology that contains all of the OWL Axioms for the given
     *         ontology.
     * @throws IllegalArgumentException
     *             If the OWLOntologyID does not contain both Ontology IRI and Version IRI.
     * @throws OWLException
     *             If there was an error while attempting to get the Ontology.
     */
    OWLOntology getOntology(OWLOntologyID ontologyID) throws IllegalArgumentException, OWLException;
    
    /**
     * @return The OWLOntologyManager mapped to this PoddOWLManager
     */
    OWLOntologyManager getOWLOntologyManager();
    
    /**
     * @return The OWLReasonerFactory mapped to this PoddOWLManager
     */
    OWLReasonerFactory getReasonerFactory();
    
    /**
     * 
     * @return The {@link OWLProfile} used by the reasoner attached to this PoddOWLManager.
     */
    OWLProfile getReasonerProfile();
    
    /**
     * Returns a list of versions where one of the ontology IRI or version IRI match the ontology.
     * 
     * The most current version of the ontology is returned first in the list.
     * 
     * NOTE: The list of {@link OWLOntologyID} instances that are returned may not all contain the
     * given IRI in cases where the given IRI is one of many different Version IRIs.
     * 
     * @param ontologyIRI
     *            Either the Ontology IRI or the Version IRI of an ontology to match against.
     * @return A list of versions for all ontologies, where the version either matches the Ontology
     *         IRI or the Version IRI.
     */
    List<OWLOntologyID> getVersions(IRI ontologyIRI);
    
    /**
     * Infer statements for the given {@link OWLOntologyID} into the given permanent repository
     * connection.
     * 
     * @param ontologyId
     * @param permanentRepositoryConnection
     * @return The InferredOWLOntologyID representing the ontologyId, along with the IRI of the
     *         Inferred Ontology.
     */
    InferredOWLOntologyID inferStatements(OWLOntologyID ontologyId, RepositoryConnection permanentRepositoryConnection);
    
    /**
     * Returns true if the given ontologyIRI is either an Ontology IRI and it has a published
     * version, or is a version IRI that was previously published.
     * 
     * @param ontologyIRI
     * @return
     */
    boolean isPublished(IRI ontologyIRI);
    
    /**
     * Returns true if the combination of the Ontology IRI and the Version IRI in the given
     * ontologyId were previously published.
     * 
     * @param ontologyId
     * @return
     */
    boolean isPublished(OWLOntologyID ontologyId);
    
    /**
     * Loads an ontology into memory from an OWLOntologyDocumentSource.
     * 
     * @param owlSource
     * @return The OWLOntology that was loaded into memory
     * @throws OWLException
     * @throws IOException
     * @throws PoddException
     */
    OWLOntology loadOntology(OWLOntologyDocumentSource owlSource) throws OWLException, IOException, PoddException;
    
    /**
     * Parses RDF statements into an ontology, and returns the OWLOntologyID for the resulting
     * ontology.
     * 
     * NOTE: The Ontology is managed by the internal OWLOntologyManager, and will still be in memory
     * after this call.
     * 
     * @param conn
     *            Repository Connection that can be used to access the RDF statements.
     * @param contexts
     *            The Contexts inside of the Repository Connection that contain the relevant RDF
     *            statements.
     * @return The OWLOntologyID that was created by the internal OWLOntologyManager for the
     *         ontology that was parsed.
     * @throws OWLException
     * @throws IOException
     * @throws PoddException
     * @throws OpenRDFException
     */
    OWLOntologyID parseRDFStatements(RepositoryConnection conn, URI... contexts) throws OWLException, IOException,
        PoddException, OpenRDFException;
    
    /**
     * Attempts to regain memory in the underlying OWLOntologyManager by removing the ontology from
     * the in-memory cache.
     * 
     * @param ontologyID
     *            The full OWLOntologyID, containing both Ontology IRI and Version IRI for the
     *            ontology to remove from the cache.
     * @return True if the ontology was in memory and it was successfully removed, and false if the
     *         ontology was not found in memory.
     * @throws IllegalArgumentException
     *             If the OWLOntologyID does not contain both Ontology IRI and Version IRI.
     * @throws OWLException
     *             If there was an error while attempting to retrieve the memory.
     */
    boolean removeCache(OWLOntologyID ontologyID) throws OWLException;
    
    /**
     * Sets the current version for the Ontology {@link IRI} in the given {@link OWLOntologyID} to
     * be the given version.
     * 
     * @param ontologyId
     * @throws IllegalArgumentException
     *             If the ontologyId does not have a version.
     */
    void setCurrentVersion(OWLOntologyID ontologyId);
    
    /**
     * Map a single OWLOntologyManager into this PoddOWLManager.
     * 
     * @param manager
     *            The manager for all PODD {@link OWLOntology} instances.
     */
    void setOWLOntologyManager(OWLOntologyManager manager);
    
    /**
     * Sets the given OWLOntologyID to be published, restricting the ability of the ontology to be
     * published again.
     * 
     * @param ontologyId
     *            The OWLOntologyID that needs to be published
     * @throws PublishArtifactException
     *             If the ontologyId could not be published.
     */
    InferredOWLOntologyID setPublished(OWLOntologyID ontologyId) throws PublishArtifactException;
    
    /**
     * Sets the {@link OWLReasonerFactory} to use when creating instances of {@link OWLReasoner} to
     * verify ontologies and infer statements based on ontologies.
     * 
     * @param reasonerFactory
     *            The reasoner factory to use for all ontologies in this PoddOWLManager.
     */
    void setReasonerFactory(OWLReasonerFactory reasonerFactory);
}
