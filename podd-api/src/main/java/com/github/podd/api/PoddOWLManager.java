/**
 * 
 */
package com.github.podd.api;

import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;
import org.semanticweb.owlapi.model.OWLException;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWLProfile;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.rio.RioMemoryTripleSource;

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
     * @param ontology
     * @param conn
     */
    void cacheSchemaOntology(InferredOWLOntologyID ontology, RepositoryConnection conn);
    
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
    
    OWLProfile getReasonerProfile();
    
    void inferStatements(InferredOWLOntologyID inferredOWLOntologyID, RepositoryConnection permanentRepositoryConnection);
    
    /**
     * Loads an ontology into memory from a RioMemoryTripleSource.
     * 
     * @param owlSource
     * @return
     */
    OWLOntology loadOntology(RioMemoryTripleSource owlSource);
    
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
     */
    OWLOntologyID parseRDFStatements(RepositoryConnection conn, URI... contexts);
    
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
     * Map a single OWLOntologyManager into this PoddOWLManager.
     * 
     * @param manager
     */
    void setOWLOntologyManager(OWLOntologyManager manager);
}
